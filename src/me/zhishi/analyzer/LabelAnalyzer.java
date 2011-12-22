package me.zhishi.analyzer;

import me.zhishi.tools.FileNames;
import me.zhishi.tools.GlobalConfigs;
import me.zhishi.tools.NTriplesReader;

public class LabelAnalyzer
{
	private NTriplesReader labelReader;
	private FileNames fileName;
	
	public static void main( String[] args )
	{
		LabelAnalyzer ana = new LabelAnalyzer( GlobalConfigs.Hudong );
		ana.driver();
		ana.closeReader();
	}
	
	public LabelAnalyzer( String source )
	{
		fileName = new FileNames( 2.0 );
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
			String label = labelReader.getBareObject();
			System.out.println( label );
		}
	}
}
