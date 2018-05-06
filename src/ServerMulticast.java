
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMulticast
{
	public static final String DEFAULT_GROUP_ADDRESS = "228.5.6.7";
	public static final int DEFAULT_REGISTRY_PORT = 5678;
	public static final int MESSAGE_SIZE_IN_BYTES = 4096;
	public static Logger logger;
	private String stationId;
	private String groupAddress;
	private int port;
	private MulticastSocket sock;
	private InetAddress group;
	private int groupSize;
	private boolean inGroup;
	private List<String> peers;

	public ServerMulticast(String stationId)
	{
		this(stationId, DEFAULT_GROUP_ADDRESS, DEFAULT_REGISTRY_PORT, null);
	}

	public ServerMulticast(String stationId, Logger logger)
	{
		this(stationId, DEFAULT_GROUP_ADDRESS, DEFAULT_REGISTRY_PORT, logger);
	}

	public ServerMulticast(String stationId, String groupAddress)
	{
		this(stationId, groupAddress, DEFAULT_REGISTRY_PORT, null);
	}

	public ServerMulticast(String stationId, String groupAddress, Logger logger)
	{
		this(stationId, groupAddress, DEFAULT_REGISTRY_PORT, logger);
	}

	public ServerMulticast(String stationId, int port)
	{
		this(stationId, DEFAULT_GROUP_ADDRESS, port, null);
	}

	public ServerMulticast(String stationId, int port, Logger logger)
	{
		this(stationId, DEFAULT_GROUP_ADDRESS, port, logger);
	}

	public ServerMulticast(String stationId, String groupAddress, int port, Logger logger)
	{
		this.stationId = stationId;
		this.groupAddress = groupAddress;
		this.port = port;
		peers = new ArrayList<>();
		if (logger == null)
			ServerMulticast.logger = Logger.getLogger(ServerMulticast.class.getName());
		else
			ServerMulticast.logger = logger;
	}

	public boolean open()
	{
		try {
			sock = new MulticastSocket(port);
			group = InetAddress.getByName(groupAddress);
			sock.joinGroup(group);
			inGroup = true;
			return true;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public void close()
	{
		leaveGroup();
		sock.close();
	}

	public boolean send(String message)
	{
		DatagramPacket pkt = new DatagramPacket(message.getBytes(),
			message.length(), group, port);
		try {
			sock.send(pkt);
			return true;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public String receive()
	{
		byte[] buf;
		DatagramPacket pkt;
		String msg;
		try {
			while (true) {
				buf = new byte[MESSAGE_SIZE_IN_BYTES];
				pkt = new DatagramPacket(buf, buf.length);
				sock.receive(pkt);
				int i;
				for (i = 0; i < buf.length && buf[i] != 0; i++);
				String str = new String(buf, 0, i, Charset.defaultCharset());
				String[] toks = str.split(":");
				if (toks[0].equals("HI")) {
					if (!toks[1].equals(stationId)) {
						if (!peers.contains(toks[1])) {
							groupSize++;
							peers.add(toks[1]);
							msg = "Connected to " + toks[1] + "StationServer.";
							System.out.println(msg);
							logger.log(Level.INFO, msg, (String[]) null);
						}
						send("HI:" + stationId);
					}
				} else if (toks[0].equals("BYE")) {
					if (!toks[1].equals(stationId) && peers.contains(toks[1])) {
						groupSize--;
						peers.remove(toks[1]);
						msg = toks[1] + "StationServer left.";
						System.out.println(msg);
						logger.log(Level.INFO, msg, (String[]) null);
					}
				} else 
					return str;
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public boolean leaveGroup()
	{
		try {
			sock.leaveGroup(group);
			inGroup = false;
			return true;
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return false;
	}

	public boolean isInGroup()
	{
		return inGroup;
	}

	public String getGroupAddress()
	{
		return groupAddress;
	}

	public void setGroupAddress(String groupAddress)
	{
		this.groupAddress = groupAddress;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public int getGroupSize()
	{
		return groupSize;
	}

	public void setGroupSize(int groupSize)
	{
		this.groupSize = groupSize;
	}
}
