package me.zhishi.tools.file;

import me.zhishi.tools.TextTools;

public class TripleWriter
{	
	public static String getTripleLine( String subject, String predicate, String object )
	{
		return subject + " " + predicate + " " + object + " .";
	}
	
	public static String getResourceObjectTriple( String subjectIRI, String predicate, String objectIRI )
	{
		return getTripleLine( "<"+subjectIRI+">", predicate, "<"+objectIRI+">" );
	}
	
	public static String getStringValueTriple( String subjectIRI, String predicate, String str )
	{
		return getTripleLine( "<"+subjectIRI+">", predicate, "\""+TextTools.getUnicode(str)+"\"@zh" );
	}
	
	public static String getValueTriple( String subjectIRI, String predicate, String value, String type )
	{
		return getTripleLine( "<"+subjectIRI+">", predicate, "\""+value+"\"^^<"+type+">" );
	}
}
