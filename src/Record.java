
import java.util.Objects;

public class Record
{
	protected String id;
	protected String firstName;
	protected String lastName;
	protected RecordType type;

	protected Record(String id, String firstName, String lastName, RecordType type)
	{
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.type = type;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.id);
		hash = 23 * hash + Objects.hashCode(this.firstName);
		hash = 23 * hash + Objects.hashCode(this.lastName);
		hash = 23 * hash + Objects.hashCode(this.type);
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Record other = (Record) obj;
		if (!Objects.equals(this.id, other.id))
			return false;
		if (!Objects.equals(this.firstName, other.firstName))
			return false;
		if (!Objects.equals(this.lastName, other.lastName))
			return false;
		if (this.type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Record {" + "id = " + id + ", firstName = " + firstName + ", "
			+ "lastName = " + lastName + ", type = " + type + '}';
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public RecordType getType()
	{
		return type;
	}

	public void setType(RecordType type)
	{
		this.type = type;
	}
}
