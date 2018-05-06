
import java.util.Date;
import java.util.Objects;

public class MissingRecord extends Record
{
	private static int numRecords;
	private String lastSeenAddress;
	private Date lastSeenDate;
	private String lastSeenLocation;
	private boolean found;

	public MissingRecord(String id, String firstName, String lastName,
		String lastSeenAddress, Date lastSeenDate, String lastSeenLocation,
		boolean found)
	{
		super(id, firstName, lastName, RecordType.MISSING);
		this.lastSeenAddress = lastSeenAddress;
		this.lastSeenDate = lastSeenDate;
		this.lastSeenLocation = lastSeenLocation;
		this.found = found;
	}

	public static MissingRecord newMissingRecord(String firstName, String lastName,
		String lastSeenAddress, Date lastSeenDate, String lastSeenLocation,
		boolean found)
	{
		String id = String.format("MR%05d", numRecords++);
		return new MissingRecord(id, firstName, lastName, lastSeenAddress,
			lastSeenDate, lastSeenLocation, found);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.lastSeenAddress);
		hash = 59 * hash + Objects.hashCode(this.lastSeenDate);
		hash = 59 * hash + Objects.hashCode(this.lastSeenLocation);
		hash = 59 * hash + (this.found ? 1 : 0);
		return hash + super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MissingRecord other = (MissingRecord) obj;
		if (!super.equals((Record) obj))
			return false;
		if (!Objects.equals(this.lastSeenAddress, other.lastSeenAddress))
			return false;
		if (!Objects.equals(this.lastSeenDate, other.lastSeenDate))
			return false;
		if (!Objects.equals(this.lastSeenLocation, other.lastSeenLocation))
			return false;
		if (this.found != other.found)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		String str = super.toString();
		str = str.substring(8, str.length() - 1);
		return "MissingRecord {" + str + ", lastSeenAddress = " + lastSeenAddress
			+ ", lastSeenDate = " + lastSeenDate + ", lastSeenLocation = "
			+ lastSeenLocation + ", found = " + found + '}';
	}

	public static int getNumRecords()
	{
		return numRecords;
	}

	public static void setNumRecords(int numRecords)
	{
		MissingRecord.numRecords = numRecords;
	}

	public String getLastSeenAddress()
	{
		return lastSeenAddress;
	}

	public void setLastSeenAddress(String lastSeenAddress)
	{
		this.lastSeenAddress = lastSeenAddress;
	}

	public Date getLastSeenDate()
	{
		return lastSeenDate;
	}

	public void setLastSeenDate(Date lastSeenDate)
	{
		this.lastSeenDate = lastSeenDate;
	}

	public String getLastSeenLocation()
	{
		return lastSeenLocation;
	}

	public void setLastSeenLocation(String lastSeenLocation)
	{
		this.lastSeenLocation = lastSeenLocation;
	}

	public boolean isFound()
	{
		return found;
	}

	public void setFound(boolean found)
	{
		this.found = found;
	}
}
