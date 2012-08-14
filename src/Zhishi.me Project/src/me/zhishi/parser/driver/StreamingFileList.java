package me.zhishi.parser.driver;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.HDFSFileWriter;

public class StreamingFileList
{
	public static void main( String[] args )
	{
		generateBZ2List( URICenter.source_name_baidu );
		generateBZ2List( URICenter.source_name_hudong );
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
