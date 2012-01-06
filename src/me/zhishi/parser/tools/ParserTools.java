package me.zhishi.parser.tools;

import java.io.IOException;

import me.zhishi.tools.HDFSFileWriter;
import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;

public class ParserTools
{
	public static void main( String[] args ) throws IOException
	{
		generateBZ2List( URICenter.source_name_hudong );
	}

	public static void generateBZ2List( String source ) throws IOException
	{
		int maxDump = 0;
		if( source.equals( URICenter.source_name_hudong ) )
			maxDump = Path.hudongMax;
		else if( source.equals( URICenter.source_name_baidu ) )
			maxDump = Path.baiduMax;
		
		Path p = new Path( 3.0, source, true );
		HDFSFileWriter fileWriter = new HDFSFileWriter( p.getMainPagePath() + "FileList.txt", false );
		
		for( int i = 0; i <= maxDump; ++i )
		{
			String archiveName = Integer.toString( i * 10000 + 1 );
			archiveName += "-";
			archiveName += Integer.toString( (i+1) * 10000 );
			System.out.println( p.getMainPagePath() + archiveName + ".tar.bz2" );
			fileWriter.writeLine( p.getMainPagePath() + archiveName + ".tar.bz2" );
		}
		
		fileWriter.close();
	}
}
