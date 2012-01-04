package me.zhishi.parser.driver;

import java.lang.reflect.Constructor;
import java.util.Scanner;

import me.zhishi.parser.Article;
import me.zhishi.parser.Parser;
import me.zhishi.tools.FileHandler;
import me.zhishi.tools.GlobalFactory;
import me.zhishi.tools.URICenter;

import org.apache.tools.tar.TarInputStream;

public class StreamingDriver
{
	public static double releaseVersion = 3.0;
	
	public static void main( String[] args ) throws Exception
	{
		Constructor<? extends Parser> constructor = null;
		
		Scanner scanner = new Scanner( System.in );
		while( scanner.hasNext() )
		{
			scanner.next();
			String archiveName = scanner.next();
			String source = null;
			if( archiveName.contains( URICenter.source_name_baidu ) )
			{
				source = URICenter.source_name_baidu;
				constructor = (new GlobalFactory()).baiduParserConstructor;
			}
			else if( archiveName.contains( URICenter.source_name_hudong ) )
			{
				source = URICenter.source_name_hudong;
				constructor = (new GlobalFactory()).hudongParserConstructor;
			}
			
			Runtime.getRuntime().exec( "rm -f tmp.tar.bz2" ).waitFor();
			Runtime.getRuntime().exec( "hadoop dfs -get " + archiveName + " tmp.tar.bz2" ).waitFor();
			
			System.err.println( source + archiveName.substring( archiveName.lastIndexOf( "/" ) ) );
			System.err.println( "reporter:status:" + source + archiveName.substring( archiveName.lastIndexOf( "/" ) ) );
			
			FileHandler fh = new FileHandler( "tmp.tar.bz2" );
			TarInputStream tin = (TarInputStream) fh.getInputStream();
			
			Parser parser = null;
			while( tin.getNextEntry() != null )
			{
				parser = constructor.newInstance( tin );

				Article article = parser.parse();
				
				for( String t : article.toTriples() )
				{
					System.out.println( t );
				}
			}

			tin.close();
			fh.close();
		}
	}
}
