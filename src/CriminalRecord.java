
import java.util.Objects;

public class CriminalRecord extends Record
{
	private static int numRecords;
	private String description;
	private boolean captured;

	public CriminalRecord(String id, String firstName, String lastName,
		String description, boolean captured)
	{
		super(id, firstName, lastName, RecordType.CRIMINAL);
		this.description = description;
		this.captured = captured;
	}

	public static CriminalRecord newCriminalRecord(String firstName, String lastName,
		String description, boolean captured)
	{
		String id = String.format("CR%05d", numRecords++);
		return new CriminalRecord(id, firstName, lastName, description, captured);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.description);
		hash = 89 * hash + (this.captured ? 1 : 0);
		return hash + super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CriminalRecord other = (CriminalRecord) obj;
		if (!super.equals((Record) other))
			return false;
		if (!Objects.equals(this.description, other.description))
			return false;
		if (this.captured != other.captured)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		String str = super.toString();
		str = str.substring(8, str.length() - 1);
		return "CriminalRecord {" + str + ", description = " + description + ", "
			+ "captured = " + captured + '}';
	}

	public static int getNumRecords()
	{
		return numRecords;
	}

	public static void setNumRecords(int numRecords)
	{
		CriminalRecord.numRecords = numRecords;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isCaptured()
	{
		return captured;
	}

	public void setCaptured(boolean captured)
	{
		this.captured = captured;
	}
	
}
