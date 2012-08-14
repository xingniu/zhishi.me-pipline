package me.zhishi.parser.tools;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.GZIPFileWriter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

public class ParserTools
{
	public static void main( String[] args ) throws Exception
	{
		superviseNTs( URICenter.source_name_hudong, 3.0, "label" );
	}
	
	public static void superviseNTs( String source, double version, String content )
	{
		Path p = new Path( version, source, false );
		NTriplesReader ntReader = new NTriplesReader( p.getNTriplesFile( content ) );
		GZIPFileWriter writer = new GZIPFileWriter( Path.projectDataPath + "temp.nt" );
		int counter = 0;
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			String sub = tr.getSubject();
			String pre = tr.getPredicate();
			String obj = tr.getObject();
//			if( str.length() >= 16 )
//			if( str.contains( "<" ) )
//			if( str.contains( "[" ) && str.endsWith( "]" ) )
//			if( str.equals( "" ) || str.contains( "null" ) )
//			if( !obj.startsWith( "http://" ) )
			if( tr.getSubjectContent().contains( "<" ) )
			{
				counter++;
				System.out.println( counter + ": " + sub + "\t" + pre + "\t" + obj );
			}
			writer.writeLine( TripleWriter.getResourceObjectTriple( "http://zhishi.me/"+tr.getObjectValue(), "", "" ) );
		}
		ntReader.close();
		writer.close();
		System.out.println( counter );
	}
}
