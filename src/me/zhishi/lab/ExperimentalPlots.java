package me.zhishi.lab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import me.zhishi.analyzer.DataAnalyzer;
import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;

public class ExperimentalPlots
{
	static LinkedList<String> list;
	
	public static void main( String[] args ) throws Exception
	{
//		list = new LinkedList<String>();
//		for( int i = 0; i < 3; ++i )
//			list.add( String.valueOf( i ) );
//		getSet( 0, 0, 0 );
		Path fileName = new Path( 3.1, URICenter.source_name_zhwiki, true );
		System.out.println( fileName.getNTriplesFile( "redirect" ) );
		NTriplesReader fileReader = new NTriplesReader( fileName.getNTriplesFile( "redirect" ) );
		FileWriter fileWriter = new FileWriter( "redirect.txt", true );
		while( fileReader.readNextLine() != null )
		{
			TripleReader tr = fileReader.getTripleReader();
			String labelA = tr.getSubjectContent();
			String labelB = tr.getObjectContent();
			fileWriter.write( labelA + "\t" + labelB + "\n" );
		}
		fileWriter.close();
		fileReader.close();
	}
	
	public static void getSet( int deep, int sum, long set )
	{
		if( deep == list.size() || sum == 1000 )
		{
			if( set != 0 )
			{
				StringBuffer is = new StringBuffer();
				for( int i = 0; i < list.size(); ++i )
				{
					if( (set & (1L << i)) > 0 )
					{
						is.append( list.get( i ) + "," );
					}
				}
				System.out.println( is );
				System.out.println( deep + " " + sum );
			}
			return;
		}
		getSet( deep + 1, sum, set );
		getSet( deep + 1, sum + 1, set | (1L << deep) );
	}
}
