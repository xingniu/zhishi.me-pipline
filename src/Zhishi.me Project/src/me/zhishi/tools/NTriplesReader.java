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
	private int SE;
	private int PE;
	private int OE;
	
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
				SE = currentLine.indexOf( "> <" ) + 1;
				PE = currentLine.indexOf( "> ", SE + 1 ) + 1;
				OE = currentLine.length() - 2;
//				if( SE<0 || PE<0 || OE<=PE )
//				{
//					System.out.println( currentLine );
//				}
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
	
	/**
	 * @return the URI of the subject
	 */
	public String getSubject()
	{
		return currentLine.substring( 0, SE );
	}
	
	/**
	 * @return the local name of the subject
	 */
	public String getDecodedSubject()
	{
		String s = getSubject();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	/**
	 * @return the URI of the predicate
	 */
	public String getPredicate()
	{
		return currentLine.substring( SE + 1, PE );
	}
	
	/**
	 * @return the local name of the predicate
	 */
	public String getDecodedPredicate()
	{
		String s = getPredicate();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	public String getBarePredicate( String start )
	{
		String s = getPredicate();
		int length = start.length();
		return s.substring( s.indexOf( start )+ length, s.lastIndexOf( ">" ) );
	}
	
	/**
	 * @return the object (URI or string)
	 */
	public String getObject()
	{
		return currentLine.substring( PE + 1, OE );
	}
	
	/**
	 * @return the local name of the object (will fail if the object is not URI)
	 */
	public String getDecodedObject()
	{
		String s = getObject();
		return TextTools.decoder( s.substring( s.lastIndexOf( "/" )+1, s.indexOf( ">" ) ) );
	}
	
	/**
	 * @return the string value of the object (will fail if the object is URI)
	 */
	public String getBareObject()
	{
		String s = getObject();
		if( s.contains( "\"^^" ) )
			return TextTools.UnicodeToString(s.substring( 1, s.indexOf( "\"^^" ) ));
		else if( s.contains( "\"@" ) )
			return TextTools.UnicodeToString(s.substring( 1, s.lastIndexOf( "\"@" ) ));
		else
			return "";
	}
	
	public String getCurrentLine()
	{
		return currentLine;
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
	
	public static void main(String args[]) {
		NTriplesReader triple = new NTriplesReader("test.nt");
		while (triple.readNextLine() != null) {
			System.out.println(triple.getSubject() + "\t" + triple.getPredicate() + "\t" + triple.getObject());
		}
		triple.close();
	}
}
