package me.zhishi.tools;

import java.io.IOException;

public class SmallTools
{
	public static void main( String[] args ) throws IOException
	{
		String s = " A ";
		System.out.println( s.trim() );
	}
	
	public static String getHadoopOutputName( String prefix, int n )
	{
		return prefix + "-r-" + String.format( "%05d", n );
	}
}
