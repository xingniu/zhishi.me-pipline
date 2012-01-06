package me.zhishi.parser.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import me.zhishi.tools.FileHandler;
import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public class FileExtractor
{
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	public static double releaseVersion = 3.0;
	
	public static int ID = 251911;
	
	public static void main( String[] args ) throws IOException
	{
		Charset charset = null;
		if( source.equals( URICenter.source_name_hudong ) )
		{
			charset = Charset.forName( "UTF-8" );
		}
		else if( source.equals( URICenter.source_name_baidu ) )
		{
			charset = Charset.forName( "GB2312" );
		}
		
		int No = ID / 10000;
		String archiveName = Integer.toString( No * 10000 + 1 );
		archiveName += "-";
		archiveName += Integer.toString( (No+1) * 10000 );
		System.out.println( archiveName );
		
		Path path = new Path( releaseVersion, source );
		FileHandler fh = new FileHandler( path.getMainPageFilePath( archiveName ) );
		TarInputStream tin = (TarInputStream) fh.getInputStream();
		
		TarEntry entry;
		while( ( entry = tin.getNextEntry() ) != null )
		{
			if( entry.getName().equals( archiveName + "/" + Integer.toString( ID ) + ".htm" ) )
			{
				System.out.println( entry.getName() );
				
				BufferedReader stringReader = new BufferedReader( new InputStreamReader( tin, charset ) );
//				FileWriter fileWriter = new FileWriter( path.getMainPagePath() + Integer.toString( ID ) + ".htm", false );
				PrintWriter fileWriter = new PrintWriter( path.getMainPagePath() + Integer.toString( ID ) + ".htm", charset.displayName());
				while( true )
				{
					String line = stringReader.readLine();
					if( line == null )
						break;
					System.out.println( line );
					fileWriter.write( line + "\n" );
				}
				fileWriter.close();
				stringReader.close();
				break;
			}
		}
		tin.close();
		fh.close();
	}
}
