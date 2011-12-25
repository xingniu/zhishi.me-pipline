package me.zhishi.parser.driver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import me.zhishi.parser.Article;
import me.zhishi.parser.HudongParser;
import me.zhishi.parser.Parser;
import me.zhishi.tools.FileHandler;
import me.zhishi.tools.IRICenter;
import me.zhishi.tools.Path;

public class ParserDriver
{
	public static String source = IRICenter.source_name_hudong;
	public static double releaseVersion = 3.0;
	
	public static void main( String[] args ) throws IOException
	{
		int maxDump = 0;
		if( source.equals( IRICenter.source_name_hudong ) )
			maxDump = Path.hudongMax;
		
		for( int i = 0; i <= maxDump; ++i )
		{
			String archiveName = Integer.toString( i * 10000 + 1 );
			archiveName += "-";
			archiveName += Integer.toString( (i+1) * 10000 );
			System.err.println( new Timestamp( new java.util.Date().getTime() ) );
			System.out.println( "Parsing " + archiveName );
			
			Path path = new Path( releaseVersion, source );
			FileHandler fh = new FileHandler( path.getMainPageFilePath( archiveName ) );
			TarInputStream tin = (TarInputStream) fh.getInputStream();
//			ZipOutputStream zipout = new ZipOutputStream( new FileOutputStream( path.getRawStructuredDataFilePath( archiveName ) ) );
//			zipout.putNextEntry( new ZipEntry( archiveName +".txt" ) );
//			OutputStreamWriter writer = new OutputStreamWriter(zipout, "UTF-8");
			
			Parser parser = null;
			TarEntry en;
			while( (en = tin.getNextEntry()) != null )
			{
				System.out.println( en.getName() );
				if ( source.equals( IRICenter.source_name_hudong ) )
					parser = new HudongParser( tin );
				
				Article article = parser.parse();
				
				for( String t : article.toTriples() )
				{
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
