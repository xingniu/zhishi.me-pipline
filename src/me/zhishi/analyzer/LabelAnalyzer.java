package me.zhishi.analyzer;

import me.zhishi.tools.Path;
import me.zhishi.tools.IRICenter;
import me.zhishi.tools.NTriplesReader;

public class LabelAnalyzer
{
	private NTriplesReader labelReader;
	private Path fileName;
	
	public static void main( String[] args )
	{
		LabelAnalyzer ana = new LabelAnalyzer( IRICenter.source_name_hudong );
		ana.driver();
		ana.closeReader();
	}
	
	public LabelAnalyzer( String source )
	{
		fileName = new Path( 2.0 );
		fileName.setSource( source );
		labelReader = new NTriplesReader( fileName.getLabelFileName() );
	}
	
	public void closeReader()
	{
		labelReader.close();
	}
	
	public void driver()
	{
		while( labelReader.readNextLine() != null )
		{
			String label = labelReader.getObjectValue();
			System.out.println( label );
		}
	}
}
