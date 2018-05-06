
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StationServerImpl implements StationServer
{
	public static final int DEFAULT_REGISTRY_PORT = 1100;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	private String stationId;
	private int port;
	private FileLogger logger;
	private ServerMulticast multicast;
	private Map<Character, List<Record>> records;
	private List<String> stationCounts;

	{
		records = new HashMap<>();
		ArrayList<Record> recs = new ArrayList<>();
		recs.add(CriminalRecord.newCriminalRecord("Joe", "Shmoe", "Robbery", true));
		recs.add(CriminalRecord.newCriminalRecord("Jane", "Smith", "Theft", false));
		records.put('S', recs);
		recs = new ArrayList<>();
		try {
			recs.add(MissingRecord.newMissingRecord("Heather", "Alba", "3330 Brand Road, "
				+ "Saskatoon, SK S7K 1W8", dateFormat.parse("08/03/13"), "Midtown Plaza", false));
			recs.add(MissingRecord.newMissingRecord("Dianne", "Armon", "3064 Dry Pine Bay Road, "
				+ "Alban, ON P0M 1A0", dateFormat.parse("09/23/13"), "French River Post", true));
			records.put('A', recs);
		} catch (ParseException ex) {
			System.out.println("Error adding sample records");
		}
	}

	public StationServerImpl(String stationId)
	{
		this(stationId, DEFAULT_REGISTRY_PORT);
	}

	public StationServerImpl(String stationId, int port)
	{
		this.stationId = stationId;
		this.port = port;
		stationCounts = new ArrayList<>();
		try {
			logger = new FileLogger(this, stationId + ".log");
		} catch (IOException | SecurityException ex) {
			System.err.println("Couldn't create log file.");
		}
	}

	public void run()
	{
		String msg = stationId + "StationServer started.";
		System.out.println(msg);
		logger.log(Level.INFO, msg, (String[]) null);
		try {
			StationServer stub = (StationServer) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.createRegistry(port);
			registry.bind(stationId + "StationServer", stub);
			msg = "Remote object registered at port " + port + ".";
			logger.log(Level.INFO, msg, (String[]) null);
			System.out.println(msg);
		} catch (RemoteException ex) {
			msg = "Could not create registry. Is the port in use? Exiting.";
			logger.log(Level.SEVERE, msg, (String[]) null);
			System.err.println(msg);
			System.exit(1);
		} catch (AlreadyBoundException ex) {
			msg = "Remote object " + stationId + "StationServer already bound. Exiting.";
			logger.log(Level.SEVERE, msg, (String[]) null);
			System.err.println(msg);
			System.exit(1);
		}
		showRecords(true, true);
		multicast = new ServerMulticast(stationId, logger);
		if (multicast.open()) {
			msg = "Connected to multicast at address " + multicast.getGroupAddress()
				+ " on port " + multicast.getPort() + ".";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
			multicast.send("HI:" + stationId);
			new Thread()
			{
				public void run()
				{
					String recv, msg;
					String[] toks;
					while (true) {
						recv = multicast.receive();
						if (recv == null)
							return;
						toks = recv.split(":");
						if (toks[0].startsWith("COUNT_REQ") && !toks[1].equals(stationId)) {
							multicast.send("COUNT_RESP:" + stationId + ":" + toks[1] + ":" + recordCount());
							msg = "Received record count request (" + recv + "). Responded.";
							System.out.println(msg);
							logger.log(Level.INFO, msg, (String[]) null);
						} else if (toks[0].startsWith("COUNT_RESP") && !toks[1].equals(stationId)) {
							if (toks[2].equals(stationId) && stationCounts.size() < multicast.getGroupSize())
								stationCounts.add(toks[1] + ":" + toks[3]);
							msg = "Received record count response (" + recv + ").";
							System.out.println(msg);
							logger.log(Level.INFO, msg, (String[]) null);
						}
					}
				}
			}.start();
		} else {
			msg = "Could not connect to multicast at address "
				+ multicast.getGroupAddress() + " on port " + multicast.getPort() + ".";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
		}
	}

	@Override
	public synchronized String createCRecord(String firstName, String lastName,
		String description, boolean captured) throws RemoteException
	{
		CriminalRecord record = CriminalRecord.newCriminalRecord(firstName,
			lastName, description, captured);
		addRecord(record);
		String msg = "Criminal record (" + record.toString() + ") added to server.";
		System.out.println(msg);
		logger.log(Level.INFO, msg, (String[]) null);
		return record.getId();
	}

	@Override
	public synchronized String createMRecord(String firstName, String lastName,
		String lastSeenAddress, String lastSeenDate, String lastSeenLocation,
		boolean found) throws RemoteException
	{
		Date dt;
		try {
			dt = dateFormat.parse(lastSeenDate);
		} catch (ParseException ex) {
			return null;
		}
		MissingRecord record = MissingRecord.newMissingRecord(firstName,
			lastName, lastSeenAddress, dt, lastSeenLocation,
			found);
		addRecord(record);
		String msg = "Missing person record (" + record.toString() + ") added to server.";
		System.out.println(msg);
		logger.log(Level.INFO, msg, (String[]) null);
		return record.getId();
	}

	@Override
	public synchronized boolean editCRecord(String recordID, boolean captured) throws RemoteException
	{
		Record record = null;
		boolean found = false;
		for (List<Record> recs : records.values()) {
			for (Record r : recs)
				if (r.getId().equals(recordID)) {
					record = r;
					found = true;
					break;
				}
			if (found)
				break;
		}
		if (!(record instanceof CriminalRecord))
			return false;
		((CriminalRecord) record).setCaptured(captured);
		String msg = "Criminal record (" + record.toString() + ") status updated.";
		System.out.println(msg);
		logger.log(Level.INFO, msg, (String[]) null);
		return true;
	}

	@Override
	public synchronized String getRecordCounts() throws RemoteException
	{
		if (multicast.isInGroup()) {
			multicast.send("COUNT_REQ:" + stationId);
			String msg = "Sent record count request to StationServers";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
			while (stationCounts.size() < multicast.getGroupSize());
			String resp = stationId + " " + recordCount()+", ";
			for (String count : stationCounts) {
				String[] toks = count.split(":");
				resp += toks[0] + " " + Integer.parseInt(toks[1]) + ", ";
			}
			stationCounts.clear();
			resp = resp.substring(0, resp.length() - 2);
			msg = "Station-wide record count retrieved (" + resp + ")";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
			return resp;
		}
		return null;
	}

	private boolean addRecord(Record record)
	{
		if (record == null)
			return false;
		Character key = record.getLastName().toLowerCase().charAt(0);
		List<Record> recs = records.get(key);
		if (recs == null) {
			ArrayList<Record> newRecs = new ArrayList<>();
			newRecs.add(record);
			records.put(key, newRecs);
		} else
			recs.add(record);
		return true;
	}

	private void showRecords(boolean toConsole, boolean toLogger)
	{
		String msg = "Initial station records: {\n";
		for (List<Record> recs : records.values())
			for (Record r : recs)
				msg += "\t" + r.toString() + "\n";
		msg += "}";
		if (toConsole)
			System.out.println(msg);
		if (toLogger)
			logger.log(Level.OFF, msg, (String[]) null);
	}

	private int recordCount()
	{
		int count = 0;
		for (List<Record> recs : records.values())
			count += recs.size();
		return count;
	}

	public void close()
	{
		try {
			LocateRegistry.getRegistry(port).unbind(stationId + "StationServer");
		} catch (RemoteException ex) {
			String msg = "Error removing " + stationId + "StationServer object from ";
			msg += "registry (" + ex.getMessage() + ").";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
		} catch (NotBoundException ex) {
			String msg = "Error removing " + stationId + "StationServer object from ";
			msg += "registry (" + ex.getMessage() + ").";
			System.out.println(msg);
			logger.log(Level.INFO, msg, (String[]) null);
		}
		if (multicast != null && multicast.isInGroup()) {
			multicast.send("BYE:" + stationId);
			multicast.close();
		}
		String msg = stationId + "StationServer shutting down.";
		logger.log(Level.INFO, msg, (String[]) null);
		System.out.println(msg);
	}

	public static void main(String[] args)
	{
		int port = DEFAULT_REGISTRY_PORT;
		String stationId = null;
		if (args.length == 0)
			printUsage();
		if (args.length >= 1) {
			if (args[0].equals("-help"))
				printUsage();
			stationId = args[0];
		}
		if (args.length >= 2)
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException ex) {
				System.err.println("Error: Illegal port number. Exiting.");
				System.exit(1);
			}
		final StationServerImpl ss = new StationServerImpl(stationId, port);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				ss.close();
			}
		}));
		ss.run();
	}

	private static void printUsage()
	{
		System.err.println("Usage: java StationServerImpl [-help] <StationID> [<port>]");
		System.err.println("\t-- <StationID> - The station identifier. The identifier is");
		System.err.println("\t\t\tthe station acronym.");
		System.err.println("\t-- <port> - The port where the registry server runs on.");
		System.err.println("If the -help flag should be used alone.");
		System.err.println("If no port is specified, it defaults to the default registry port (1100).");
		System.exit(0);
	}
}
