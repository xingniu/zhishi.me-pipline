package me.zhishi.tools.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tools.bzip2.CBZip2InputStream;

public class NTriplesReader
{
	private InputStream fin;
	private BufferedReader stringReader;
	private String currentLine;
	private TripleReader tripleReader;
	
	public NTriplesReader( String path )
	{
		if( path.toLowerCase().startsWith( "/users" ) )
		{
			if( !loadHDFSFile( path ) )
				System.err.println( "Error found!" );
		}
		else if( path.toLowerCase().endsWith( ".zip" ) )
		{
			if( !loadZipFile( path ) )
				System.err.println( "An error was found in loading " + path );
		}
		else if( path.toLowerCase().endsWith( ".bz2" ) )
		{
			// TODO
		}
		else 
		{
			if( loadZipFile( path + ".zip" ) )
				System.out.println( "Zipped file was found, using it instaed." );
			else if( !loadTextFile( path ) )
				System.err.println(  path + " was not found!");
		}
	}
	
	private boolean loadHDFSFile( String path )
	{
		Configuration conf = new Configuration();
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		try
		{
			FileSystem fs = FileSystem.get( URI.create( path ), conf );
			stringReader = new BufferedReader( new InputStreamReader( fs.open( new Path( path ) ) ) );
			System.out.println( "Loading " + path );
			return true;
		}
		catch( IOException e )
		{
			return false;
		}
	}
	
	private boolean loadZipFile( String path )
	{
		try
		{
			fin = new ZipInputStream( new FileInputStream( path ) );
			((ZipInputStream) fin).getNextEntry();
			stringReader = new BufferedReader( new InputStreamReader( fin, Charset.forName( "UTF-8" ) ) );
			System.out.println( "Loading " + path );
			return true;
		}
		catch( FileNotFoundException e )
		{
			return false;
		}
		catch( IOException e )
		{
			System.err.println( path + " has no entries!" );
			return false;
		}
	}
	
	private boolean loadTextFile( String path )
	{
		try
		{
			fin = new FileInputStream( path );
			stringReader = new BufferedReader( new InputStreamReader( fin, Charset.forName( "UTF-8" ) ) );
			System.out.println( "Loading " + path );
			return true;
		}
		catch( FileNotFoundException e )
		{
			return false;
		}
	}
	
	public NTriplesReader( ZipInputStream fin )
	{
		this.fin = fin;
		try
		{
			fin.getNextEntry();
		}
		catch( IOException e )
		{
			System.err.println( "No entries!" );
		}
		stringReader = new BufferedReader( new InputStreamReader( fin, Charset.forName( "UTF-8" ) ) );
	}

	public NTriplesReader( CBZip2InputStream fin )
	{
		this.fin = fin;
		stringReader = new BufferedReader( new InputStreamReader( fin, Charset.forName("UTF-8") ) );
	}

	/**
	 * Call this method to move to the next line.
	 * @return
	 */
	public String readNextLine()
	{
		try
		{
			do
			{
				currentLine = stringReader.readLine();
			} while( currentLine != null && !currentLine.startsWith( "<" ) );
			if( currentLine != null )
			{
				tripleReader = new TripleReader( currentLine );
			}
			return currentLine;
		}
		catch( NullPointerException e )
		{
			System.out.println( currentLine );
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public TripleReader getTripleReader()
	{
		return tripleReader;
	}
	
	public void close()
	{
		try
		{
			fin.close();
			stringReader.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
