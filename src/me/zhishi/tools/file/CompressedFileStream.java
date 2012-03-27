package me.zhishi.tools.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

public class CompressedFileStream
{
	private InputStream is;
	
	public CompressedFileStream( String fileName )
	{
		InputStream fileIS;
		try
		{
			fileIS = new FileInputStream( fileName );
			if( fileName.endsWith( ".bz2" ) )
			{
				fileIS.read();
				fileIS.read();
				is = new CBZip2InputStream( fileIS );
			}
			else if( fileName.endsWith( ".gz" ) )
			{
				is = new GZIPInputStream( fileIS );
			}
			else if( fileName.endsWith( ".zip" ) )
			{
				is = new ZipInputStream( fileIS );
				((ZipInputStream) is).getNextEntry();
			}
			else
			{
				System.err.println( "Unknown format!" );
			}
		}
		catch( FileNotFoundException e )
		{
			System.err.println( "File not found!" );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public InputStream getInputStream()
	{
		return is;
	}
	
	public void close()
	{
		try
		{
			is.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
