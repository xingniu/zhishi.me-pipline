package me.zhishi.tools;

public class StringPair
{
	public String first;
	public String second;
	
	public StringPair()
	{
		first = second = null;
	}

	public StringPair( String first, String second )
	{
		this.first = first;
		this.second = second;
	}

	public static boolean same( String first, String second )
	{
		return first == null ? second == null : first.equals( second );
	}

	String getFirst()
	{
		return first;
	}

	String getSecond()
	{
		return second;
	}

	void setFirst( String first )
	{
		this.first = first;
	}

	void setSecond( String second )
	{
		this.second = second;
	}

	public String toString()
	{
		return "<" + first + ", " + second + ">";
	}
}
