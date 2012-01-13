package me.zhishi.parser.tools;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.HDFSFileReader;
import me.zhishi.tools.file.ZIPFileWriter;

public class CopyFiles
{
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	public static String[] contents = {
//					"label",
//					"category",
//					"abstract",
//					"externalLink",
//					"relatedPage",
//					"internalLink",
//					"redirect",
//					"disambiguation",
//					"articleLink",
					};
	
	public static void main( String[] args )
	{
		fromHDFStoLocalZip();
	}
	
	public static void fromHDFStoLocalZip()
	{
		for( String c : contents )
		{
			Path hp = new Path( 3.0, source, true );
			HDFSFileReader hReader = new HDFSFileReader( hp.getFilePath( c ) );
			Path pp = new Path( 3.0, source, false );
			ZIPFileWriter zWriter = new ZIPFileWriter( pp.getNTriplesPath(), pp.getFileName( c ) );
			
			System.out.println( "Copying " + pp.getFileName( c ) );
			
			String line = null;
			while( (line = hReader.readLine()) != null )
			{
//				System.out.println( line );
				zWriter.writeLine( line );
			}
			hReader.close();
			zWriter.close();
		}
	}
}
