package me.zhishi.tools.file;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPFileWriter
{
	private ZipOutputStream zipout;
	private OutputStreamWriter writer;
	
	public ZIPFileWriter( String directory, String fileName )
	{
		try
		{
			zipout = new ZipOutputStream( new FileOutputStream( directory + fileName + ".zip" ) );
			zipout.putNextEntry( new ZipEntry( fileName ) );
			writer = new OutputStreamWriter( zipout, "UTF-8" );
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
			zipout.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
