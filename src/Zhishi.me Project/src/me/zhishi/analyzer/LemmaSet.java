package me.zhishi.analyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.zhishi.tools.Path;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;

public class LemmaSet
{
	public static void main( String[] args )
	{
		LemmaSet ls = new LemmaSet( 3.0 );
//		ls.getUnion();
		System.out.println( ls.getIntersection( 2 ).size() );
	}
	
	private double releaseVersion;
	private boolean fromHDFS;

	public LemmaSet( double releaseVersion )
	{
		this.releaseVersion = releaseVersion;
		fromHDFS = false;
	}
	
	public LemmaSet( double releaseVersion, boolean fromHDFS )
	{
		this.releaseVersion = releaseVersion;
		this.fromHDFS = fromHDFS;
	}
	
	public HashSet<String> getUnion()
	{
		HashSet<String> union = new HashSet<String>();
		Path p;
		NTriplesReader ntReader;
		
		p = new Path( releaseVersion, URICenter.source_name_baidu, fromHDFS );
		ntReader = new NTriplesReader( p.getFilePath( "label" ) );
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			union.add( tr.getSubjectContent() );
		}
		ntReader.close();
		
		p = new Path( releaseVersion, URICenter.source_name_hudong, fromHDFS );
		ntReader = new NTriplesReader( p.getFilePath( "label" ) );
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			union.add( tr.getSubjectContent() );
		}
		ntReader.close();
		
//		p = new Path( releaseVersion, URICenter.source_name_zhwiki, fromHDFS );
//		ntReader = new NTriplesReader( p.getLabelFileName() );
//		while( ntReader.readNextLine() != null )
//		{
//			TripleReader tr = ntReader.getTripleReader();
//			intersection.add( tr.getSubjectContent() );
//		}
//		ntReader.close();
		
		return union;
	}
	
	public HashSet<String> getIntersection( int num )
	{
		HashSet<String> intersection = new HashSet<String>();
		HashMap<String,Integer> lemmaCounter = new HashMap<String,Integer>();
		Path p;
		NTriplesReader ntReader;
		
		p = new Path( releaseVersion, URICenter.source_name_baidu, fromHDFS );
		ntReader = new NTriplesReader( p.getFilePath( "label" ) );
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			lemmaCounter.put( tr.getSubjectContent(), 1 );
		}
		ntReader.close();
		
		p = new Path( releaseVersion, URICenter.source_name_hudong, fromHDFS );
		ntReader = new NTriplesReader( p.getFilePath( "label" ) );
		while( ntReader.readNextLine() != null )
		{
			TripleReader tr = ntReader.getTripleReader();
			String label = tr.getSubjectContent();
			if( lemmaCounter.containsKey( label ) )
				lemmaCounter.put( label, 2 );
			else
				lemmaCounter.put( label, 1 );
				
		}
		ntReader.close();
		
//		p = new Path( releaseVersion, URICenter.source_name_zhwiki, fromHDFS );
//		ntReader = new NTriplesReader( p.getLabelFileName() );
//		while( ntReader.readNextLine() != null )
//		{
//			TripleReader tr = ntReader.getTripleReader();
//			String label = tr.getSubjectContent();
//			if( lemmaCounter.containsKey( label ) )
//				lemmaCounter.put( label, lemmaCounter.get( label ) + 1 );
//			else
//				lemmaCounter.put( label, 1 );
//				
//		}
//		ntReader.close();
		
		for( Entry<String,Integer> entry : lemmaCounter.entrySet() )
		{
			if( entry.getValue() >= num )
				intersection.add( entry.getKey() );
		}
		
		return null;
	}
}
