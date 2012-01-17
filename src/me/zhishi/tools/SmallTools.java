package me.zhishi.tools;

import java.io.IOException;

import me.zhishi.tools.file.GZIPFileWriter;
import me.zhishi.tools.file.NTriplesReader;

public class SmallTools
{
	public static double releaseVersion = 3.0;
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	public static String[] contents = {
//		"label",
//		"category",
//		"abstract",
//		"externalLink",
//		"relatedPage",
//		"internalLink",
//		"redirect",
//		"disambiguation",
//		"articleLink",
//		"image",
//		"imageInfo",
//		"infobox",
//		"categoryLabel",
//		"propertyLabel",
		//2.9
//		"baiduLink",
//		"hudongLink",
//		"zhwikiLink",
		};
	
	public static void main( String[] args ) throws IOException
	{
		for( String c : contents )
		{
			fromZIPtoGZIP( c );
		}
	}
	
	public static String getHadoopOutputName( String prefix, int n )
	{
		return prefix + "-r-" + String.format( "%05d", n );
	}
	
	public static void fromZIPtoGZIP( String content )
	{
		Path p = new Path( releaseVersion, source );
		System.out.println( "Converting " + p.getNTriplesFile( content ) );
		NTriplesReader reader = new NTriplesReader( p.getNTriplesFile( content ) );
		GZIPFileWriter writer = new GZIPFileWriter( p.getNTriplesFile( content ) );
		String line;
		while( (line = reader.readNextLine()) != null )
		{
			writer.writeLine( line );
		}
		writer.close();
		reader.close();
	}
}
