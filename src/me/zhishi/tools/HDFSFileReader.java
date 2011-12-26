package me.zhishi.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSFileReader
{
	private BufferedReader reader;
	
	public HDFSFileReader( String path )
	{
		Configuration conf = new Configuration();
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		try
		{
			FileSystem fs = FileSystem.get( URI.create( path ), conf );
			reader = new BufferedReader( new InputStreamReader( fs.open( new Path( path ) ) ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public String readLine()
	{
		try
		{
			return reader.readLine();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void close()
	{
		try
		{
			reader.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
