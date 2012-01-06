package me.zhishi.parser.tools;

import me.zhishi.tools.HDFSFileReader;
import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;

public class MovingFiles
{
	public static void main( String[] args )
	{
		Path hp = new Path( 3.0, URICenter.source_name_hudong, true );
		System.out.println( hp.getFileName( "label" ) );
		HDFSFileReader hReader = new HDFSFileReader( hp.getFileName( "label" ) );
		String line = null;
		while( (line = hReader.readLine()) != null )
		{
			System.out.println( line );
		}
		hReader.close();
	}
}
