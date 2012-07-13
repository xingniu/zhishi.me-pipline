package me.zhishi.analyzer;

import me.zhishi.tools.Path;
import me.zhishi.tools.file.NTriplesReader;

public class DataAnalyzer
{

	protected NTriplesReader fileReader;
	protected Path fileName;
	
	public DataAnalyzer( double version, String source, String type )
	{
		fileName = new Path( version, source );
		fileReader = new NTriplesReader( fileName.getNTriplesFile( type ) );
	}

	public void closeReader()
	{
		fileReader.close();
	}
}
