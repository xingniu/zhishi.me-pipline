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
//		superviseNTs( URICenter.source_name_hudong, 3.0, "imageInfo" );
	}
	
	public static void superviseNTs( String source, double version, String content )
	{
		Path p = new Path( version, source, false );
		NTriplesReader ntReader = new NTriplesReader( p.getNTriplesFile( content ) );
		int counter = 0;
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			String sub = tr.getSubject();
			String pre = tr.getPredicate();
			String obj = tr.getObject();
			if( pre.equals( URICenter.predicate_dc_rights ) )
			{
				sub = sub.substring( 1, sub.length() - 1 );
				obj = obj.substring( 1, obj.length() - 1 );
				try
				{
					URI uri1 = new URI( sub );
					URI uri2 = new URI( obj );
				}
				catch( URISyntaxException e )
				{
//					System.out.println( sub + "\t" + pre + "\t" + obj );
//					e.printStackTrace();
					counter++;
				}
			}
//			if( str.length() >= 16 )
//			if( str.contains( "<" ) )
//			if( str.contains( "[" ) && str.endsWith( "]" ) )
//			if( str.equals( "" ) || str.contains( "null" ) )
//			if( !obj.startsWith( "http://" ) )
//			if( sub.equals( "四川北方硝化棉股份有限公司" ) )
//				System.out.println( sub + "\t" + pre + "\t" + obj );
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
