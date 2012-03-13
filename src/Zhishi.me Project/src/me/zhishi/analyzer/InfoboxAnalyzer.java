package me.zhishi.analyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;

public class InfoboxAnalyzer extends DataAnalyzer
{
	public static void main( String[] args )
	{
		InfoboxAnalyzer ana = new InfoboxAnalyzer( 3.0, URICenter.source_name_baidu );
		ana.driver();
		ana.closeReader();
	}
	
	public InfoboxAnalyzer( double version, String source )
	{
		super( version, source, "infobox" );
	}
	
	public void driver()
	{
		while( fileReader.readNextLine() != null )
		{
			TripleReader tr = fileReader.getTripleReader();
			String lemma = tr.getSubjectContent();
			String property = tr.getPredicateContent();
			String value = tr.getObjectValue();
			
//			System.out.print( lemma + " <" + property + "> " );
			
			if( !value.contains( "。" ) )
			{
				String[] valueSegs;
				if( value.contains( "、" ) )
				{
					valueSegs = value.split( "、" );
				}
				else if( value.contains( "，" ) )
				{
					valueSegs = value.split( "，" );
				}
				else
				{
					valueSegs = new String[1];
					valueSegs[0] = value;
				}
				
				int listLength = valueSegs.length;
				if( listLength > 1 )
				{
					System.out.print( lemma + " <" + property + "> " );
					
					for( int i = 0; i < listLength; ++i )
					{
						if( i == listLength - 1 )
							valueSegs[i] = valueSegs[i].replaceAll( "等", "" );
						valueSegs[i] = valueSegs[i].trim();
						System.out.print( "【" + valueSegs[i] + "】");
					}
					System.out.println();
				}
				else
				{
					
				}
			}
			else
			{
				// A sentence.
			}
		}
	}
}
