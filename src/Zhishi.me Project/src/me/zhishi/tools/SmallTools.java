package me.zhishi.tools;

import java.io.IOException;

public class SmallTools
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
	
	public static String getHadoopOutputName( String prefix, int n )
	{
		return prefix + "-r-" + String.format( "%05d", n );
	}
}
