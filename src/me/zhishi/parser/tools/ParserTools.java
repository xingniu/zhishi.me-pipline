package me.zhishi.parser.tools;

import java.net.URI;
import java.net.URISyntaxException;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.HDFSFileWriter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;

public class ParserTools
{
	public static void main( String[] args ) throws Exception
	{
//		generateBZ2List( URICenter.source_name_hudong );
		superviseNTs( URICenter.source_name_hudong, 3.0, "infobox" );
	}
	
	public static void superviseNTs( String source, double version, String content )
	{
		Path p = new Path( version, source, true );
		NTriplesReader ntReader = new NTriplesReader( p.getNTriplesFile( content ) );
		int counter = 0;
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			String sub = tr.getSubject();
			String pre = tr.getPredicate();
			String obj = tr.getObjectValue();
//			if( str.length() >= 16 )
//			if( str.contains( "<" ) )
//			if( str.contains( "[" ) && str.endsWith( "]" ) )
//			if( str.equals( "" ) || str.contains( "null" ) )
//			if( !obj.startsWith( "http://" ) )
			if( sub.equals( "<http://zhishi.me/hudongbaike/resource/%E7%BE%8E%E5%9B%BD>" ) )
			{
				counter++;
				System.out.println( sub + "\t" + pre + "\t" + obj );
			}
		}
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
