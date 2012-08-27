package me.zhishi.parser.driver;

import me.zhishi.parser.workshop.InfoboxRefiner;
import me.zhishi.tools.URICenter;

public class WorkshopDriver
{
	public static void main( String[] args ) throws Exception
	{
		double releaseVersion;
		String source;
		
		releaseVersion = 3.0;
		source = URICenter.source_name_hudong;
//		InfoboxRefiner.output( source, releaseVersion );
//		InfoboxRefiner.run( source, releaseVersion );
		source = URICenter.source_name_baidu;
//		InfoboxRefiner.output( source, releaseVersion );
//		InfoboxRefiner.run( source, releaseVersion );
	}
}
