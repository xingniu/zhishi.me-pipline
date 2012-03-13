package me.zhishi.analyzer;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;

public class LabelAnalyzer extends DataAnalyzer
{
	public static void main( String[] args )
	{
		LabelAnalyzer ana = new LabelAnalyzer( 3.0, URICenter.source_name_baidu );
		ana.driver();
		ana.closeReader();
	}
	
	public LabelAnalyzer( double version, String source )
	{
		super( version, source, "label" );
	}
	
	public void driver()
	{
		while( fileReader.readNextLine() != null )
		{
			TripleReader tr = fileReader.getTripleReader();
			String label = tr.getObjectValue();
			System.out.println( label );
		}
	}
}
