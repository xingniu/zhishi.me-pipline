package me.zhishi.parser.driver;

import me.zhishi.parser.workshop.InfoboxRefiner;
import me.zhishi.parser.workshop.PreprocessDBpedia;
import me.zhishi.parser.workshop.PropertyRestriction;
import me.zhishi.parser.workshop.SortByPredicate;
import me.zhishi.tools.URICenter;

public class WorkshopDriver
{
	public static void main( String[] args ) throws Exception
	{
		double releaseVersion;
		
		releaseVersion = 3.2;
		SortByPredicate.run( URICenter.source_name_hudong, releaseVersion );
		InfoboxRefiner.run( URICenter.source_name_hudong, releaseVersion );
		PropertyRestriction.run( URICenter.source_name_hudong, releaseVersion );
		
//		releaseVersion = 3.0;
//		SortByPredicate.run( URICenter.source_name_baidu, releaseVersion );
//		InfoboxRefiner.run( URICenter.source_name_baidu, releaseVersion );
//		PropertyRestriction.run( URICenter.source_name_baidu, releaseVersion );

//		releaseVersion = 3.1;
//		PreprocessDBpedia.run( releaseVersion );
//		PropertyRestriction.run( URICenter.source_name_zhwiki, releaseVersion );
	}
}
