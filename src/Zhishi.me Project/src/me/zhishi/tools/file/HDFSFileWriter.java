package me.zhishi.tools.file;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSFileWriter
{
	private OutputStreamWriter writer;
	
	public HDFSFileWriter( String path, boolean append )
	{
		Configuration conf = new Configuration();
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
		try
		{
			FileSystem fs = FileSystem.get( URI.create( path ), conf, me.zhishi.tools.Path.hdfs_username );
			if( append )
				writer = new OutputStreamWriter( fs.append( new Path( path ) ), "UTF-8" );
			else
				writer = new OutputStreamWriter( fs.create( new Path( path ) ), "UTF-8" );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
	
	public HDFSFileWriter( String path )
	{
		this( path, false );
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
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
