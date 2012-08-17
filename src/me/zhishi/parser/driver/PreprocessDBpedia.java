package me.zhishi.parser.driver;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.NTriplesReader;

public class PreprocessDBpedia
{
	public static double releaseVersion = 3.1;
	
	public static void main( String[] args )
	{
	}
	
	public static void observeNTs()
	{
		Path p = new Path( releaseVersion, URICenter.source_name_zhwiki );
		String base = p.getDumpPath() + "NTriples_dbpedia/";
		NTriplesReader reader = new NTriplesReader( base + "interlanguage_links_same_as_zh.nt" );
		while( reader.readNextLine() != null )
		{
			System.out.println( reader.getCurrentLine() );
		}
		reader.close();
	}
}
