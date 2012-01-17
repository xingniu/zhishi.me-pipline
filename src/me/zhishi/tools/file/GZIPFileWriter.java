package me.zhishi.tools.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

public class GZIPFileWriter
{
	private GZIPOutputStream gzipout;
	private OutputStreamWriter writer;
	
	public GZIPFileWriter( String filePath )
	{
		try
		{
			gzipout = new GZIPOutputStream( new FileOutputStream( filePath + ".gz" ) );
			writer = new OutputStreamWriter( gzipout, "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public GZIPFileWriter( String directory, String fileName )
	{
		this( directory + fileName );
	}
	
	public void write( String text )
	{
		try
		{
			writer.write( text );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public void writeLine( String text )
	{
		write( text + "\n" );
	}
	
	public void close()
	{
		try
		{
			writer.close();
			gzipout.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
