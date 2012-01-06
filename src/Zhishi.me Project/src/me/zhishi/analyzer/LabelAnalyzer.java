package me.zhishi.analyzer;

import me.zhishi.tools.Path;
import me.zhishi.tools.TripleReader;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.NTriplesReader;

public class LabelAnalyzer
{
	private NTriplesReader labelReader;
	private Path fileName;
	
	public static void main( String[] args )
	{
		LabelAnalyzer ana = new LabelAnalyzer( URICenter.source_name_hudong );
		ana.driver();
		ana.closeReader();
	}
	
	public LabelAnalyzer( String source )
	{
		fileName = new Path( 2.0 );
		fileName.setSource( source );
		labelReader = new NTriplesReader( fileName.getFileName( "label" ) );
	}
	
	public void closeReader()
	{
		labelReader.close();
	}
	
	public void driver()
	{
		while( labelReader.readNextLine() != null )
		{
			TripleReader tr = labelReader.getTripleReader();
			String label = tr.getObjectValue();
			System.out.println( label );
		}
	}
}
