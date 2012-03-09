package me.zhishi.parser.tools;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.GZIPFileWriter;
import me.zhishi.tools.file.HDFSFileWriter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

public class ParserTools
{
	public static void main( String[] args ) throws Exception
	{
//		generateBZ2List( URICenter.source_name_hudong );
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

	public static void generateBZ2List( String source )
	{
		int maxDump = 0;
		if( source.equals( URICenter.source_name_hudong ) )
			maxDump = Path.hudongMax;
		else if( source.equals( URICenter.source_name_baidu ) )
			maxDump = Path.baiduMax;
		
		Path p = new Path( 3.0, source, true );
		HDFSFileWriter fileWriter = new HDFSFileWriter( p.getMainPageFolder() + "FileList.txt", false );
		
		for( int i = 0; i <= maxDump; ++i )
		{
			String archiveName = Integer.toString( i * 10000 + 1 );
			archiveName += "-";
			archiveName += Integer.toString( (i+1) * 10000 );
			System.out.println( p.getMainPageFolder() + archiveName + ".tar.bz2" );
			fileWriter.writeLine( p.getMainPageFolder() + archiveName + ".tar.bz2" );
		}
		
		fileWriter.close();
	}
}
