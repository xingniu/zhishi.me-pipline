package me.zhishi.parser.driver;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import me.zhishi.parser.Article;
import me.zhishi.parser.Parser;
import me.zhishi.tools.FileHandler;
import me.zhishi.tools.GlobalFactory;
import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.Path;

public class ParserDriver
{
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	public static double releaseVersion = 3.0;
	
	public static void main( String[] args ) throws Exception
	{
		Constructor<? extends Parser> constructor = null;
		
		int maxDump = 0;
		if( source.equals( URICenter.source_name_hudong ) )
		{
			maxDump = Path.hudongMax;
			constructor = (new GlobalFactory()).hudongParserConstructor;
		}
		else if( source.equals( URICenter.source_name_baidu ) )
		{
			maxDump = Path.baiduMax;
			constructor = (new GlobalFactory()).baiduParserConstructor;
		}
		
		for( int i = 25; i <= maxDump; ++i )
		{
			String archiveName = Integer.toString( i * 10000 + 1 );
			archiveName += "-";
			archiveName += Integer.toString( (i+1) * 10000 );
			System.err.println( new Timestamp( new java.util.Date().getTime() ) );
			System.out.println( "Parsing " + source + ": " + archiveName );
			
			Path path = new Path( releaseVersion, source );
			FileHandler fh = new FileHandler( path.getMainPageFilePath( archiveName ) );
			TarInputStream tin = (TarInputStream) fh.getInputStream();
//			ZipOutputStream zipout = new ZipOutputStream( new FileOutputStream( path.getRawStructuredDataFilePath( archiveName ) ) );
//			zipout.putNextEntry( new ZipEntry( archiveName + ".txt" ) );
//			OutputStreamWriter writer = new OutputStreamWriter( zipout, "UTF-8" );
			
			Parser parser = null;
			TarEntry entry;
			while( (entry = tin.getNextEntry()) != null )
			{
//				System.out.println( en.getName() );
				
				parser = constructor.newInstance( tin );
				
				Article article;
				try
				{
					article = parser.parse();
				}
				catch( Exception e )
				{
					System.out.println( "<" + entry.getName() + "> <exception> \"" + TextTools.getUnicode( e.toString() ) + "\"@zh ." );
					continue;
				}
				
				for( String t : article.toTriples() )
				{
//					writer.write( t + "\n" );
					System.out.println( t );
				}
			}

			tin.close();
			fh.close();
//			writer.close();
//			zipout.close();
		}
	}
}
