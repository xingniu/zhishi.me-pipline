package me.zhishi.tools;

public class TripleReader
{
	private String triple;
	
	private int SE;
	private int PE;
	private int OE;
	
	public TripleReader( String triple )
	{
		this.triple = triple;
		SE = triple.indexOf( "> <" ) + 1;
		PE = triple.indexOf( "> ", SE + 1 ) + 1;
		OE = triple.length() - 2;
	}
	
	/**
	 * @return the URI of the subject
	 */
	public String getSubject()
	{
		return triple.substring( 0, SE );
	}
	
	/**
	 * @return the local name of the subject
	 */
	public String getDecodedSubject()
	{
		String s = getSubject();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	/**
	 * @return the URI of the predicate
	 */
	public String getPredicate()
	{
		return triple.substring( SE + 1, PE );
	}
	
	/**
	 * @return the local name of the predicate
	 */
	public String getDecodedPredicate()
	{
		String s = getPredicate();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	public String getBarePredicate( String start )
	{
		String s = getPredicate();
		int length = start.length();
		return s.substring( s.indexOf( start )+ length, s.lastIndexOf( ">" ) );
	}
	
	/**
	 * @return the object (URI or string)
	 */
	public String getObject()
	{
		return triple.substring( PE + 1, OE );
	}
	
	/**
	 * @return the local name of the object (will fail if the object is not URI)
	 */
	public String getDecodedObject()
	{
		String s = getObject();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	/**
	 * @return the string value of the object (will fail if the object is URI)
	 */
	public String getBareObject()
	{
		String s = getObject();
		if( s.contains( "\"^^<" ) )
			return TextTools.UnicodeToString(s.substring( 1, s.indexOf( "\"^^<" ) ));
		else if( s.contains( "\"@" ) )
			return TextTools.UnicodeToString(s.substring( 1, s.lastIndexOf( "\"@" ) ));
		else
			return "";
	}
}
