package me.zhishi.tools.file;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarInputStream;

public class BZ2FileHandler
{
	private String fileName;
	private FileInputStream fileIn;
	
	public BZ2FileHandler( String fileName )
	{
		this.fileName = fileName;
	}
	
	public FilterInputStream getInputStream() throws IOException
	{
		fileIn = new FileInputStream( fileName );
		if( fileName.endsWith( ".tar.bz2" ))
		{
			fileIn.read();
			fileIn.read();
			TarInputStream tin = new TarInputStream( new CBZip2InputStream( fileIn ) );
			return tin;
		}
		return null;
	}
	
	public void close()
	{
		try
		{
			fileIn.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
