package me.zhishi.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

public class NTriplesReader
{
	private String filepath;
	private InputStream fin;
	private BufferedReader stringReader;
	private String currentLine;
	private TripleReader tripleReader;
	
	public NTriplesReader( String path )
	{
		filepath = path;
		try
		{
			fin = new FileInputStream( filepath );
			stringReader = new BufferedReader( new InputStreamReader( fin, "UTF-8" ) );
		}
		catch (FileNotFoundException e)
		{
			System.err.println( filepath + " is not found!" );
		}
		catch (UnsupportedEncodingException e)
		{
			System.err.println( "UTF-8 is not supported!" );
		}
	}
	
	public NTriplesReader( ZipInputStream fin )
	{
		this.fin = fin;
		try
		{
			fin.getNextEntry();
		}
		catch (IOException e)
		{
			System.err.println( "No entries!" );
		}
		stringReader = new BufferedReader( new InputStreamReader( fin, Charset.forName("UTF-8") ) );
	}

	public NTriplesReader( CBZip2InputStream fin )
	{
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
		catch (NullPointerException e)
		{
			System.out.println( currentLine );
			e.printStackTrace();
		}
		catch (IOException e)
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
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
