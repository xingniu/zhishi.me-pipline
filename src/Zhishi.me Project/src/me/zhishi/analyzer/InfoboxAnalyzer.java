package me.zhishi.analyzer;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;

public class InfoboxAnalyzer extends DataAnalyzer
{
	public static void main( String[] args ) throws IOException
	{
		InfoboxAnalyzer ana = new InfoboxAnalyzer( 3.0, URICenter.source_name_hudong );
		ana.driver();
		ana.closeReader();
	}
	
	public InfoboxAnalyzer( double version, String source )
	{
		super( version, source, "infobox" );
	}
	
	public void driver() throws IOException
	{
		FileOutputStream fos=new FileOutputStream("map.txt");
		OutputStreamWriter osw=new OutputStreamWriter(fos);
		BufferedWriter bw=new BufferedWriter(osw);
		

		FileOutputStream fos1=new FileOutputStream("original.txt");
		OutputStreamWriter osw1=new OutputStreamWriter(fos1);
		BufferedWriter bw1=new BufferedWriter(osw1);
		
		while( fileReader.readNextLine() != null )
		{
			TripleReader tr = fileReader.getTripleReader();
			String lemma = tr.getSubjectContent();
			String property = tr.getPredicateContent();
			String value = tr.getObjectValue();
			
			bw1.write( lemma + " <" + property + "> " );
			bw1.write( "【" + value + "】");
			bw1.newLine();
			
			for( String seg : segement(lemma, value) )
			{
				bw.write( lemma + " <" + property + "> " );
				bw.write( "【" + seg + "】");
				bw.newLine();
			}
		}
		
		bw1.close();
		bw.close();
	}

	public static LinkedList<String> segement(String lemma, String value) {
		LinkedList<String> ls = new LinkedList<String>();
		if (value.equals(lemma))
		{
			ls.add(value);
			return ls; 
		}
		if( !value.contains( "。" ) )
		{
			String[] valueSegs;
			value = value.replaceAll( "\\([^\\(\\)]*\\)", "" );
			value = value.replaceAll( "（[^（）]*）", "" );
			value = value.replaceAll( "\\([^（）]*）", "" );
			value = value.replaceAll( "（[^（）]*\\)", "" );
			value = value.replaceAll( " 著 ", "，" );
			value = value.replaceAll( " 译 ", "，" );
			value = value.replaceAll( " 编 ", "，" );
			if( value.contains( "|" ) )
			{
				value = value.replaceAll("\\|", "，");
			}
			else if( value.contains( "/" ) )
			{
				value = value.replaceAll("\\/", "，");
			}
			if( value.contains( "，" ) || value.contains( "、" ) || value.contains( "；" ) )
			{
				valueSegs = value.split( "[，、；]" );
			}
			else
			{
				valueSegs = new String[1];
				valueSegs[0] = value;
			}
			
			int listLength = valueSegs.length;
				
			for( int i = 0; i < listLength; ++i )
			{
				if( i == 0 ) {
					if (valueSegs[i].indexOf(":") == 0) valueSegs[i] = valueSegs[i].replaceFirst( ":", "" );
					if (valueSegs[i].indexOf("：") == 0) valueSegs[i] = valueSegs[i].replaceFirst( "：", "" );
				}
				valueSegs[i] = valueSegs[i].replaceAll( "组织编制$", "" );
				valueSegs[i] = valueSegs[i].replaceAll( "等注释$", "" );
				valueSegs[i] = valueSegs[i].replaceAll( "(　)?[等著译编](著)?$", "" );

				valueSegs[i] = valueSegs[i].trim();
				if (  !valueSegs[i].equals("")  ) ls.add(valueSegs[i]);
			}
		}
		else
		{
			// A sentence.
		}
		return ls;
	}
}
