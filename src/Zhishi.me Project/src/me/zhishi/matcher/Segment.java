package me.zhishi.matcher;

import java.util.HashSet;

import me.zhishi.tools.Path;
import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.HDFSFileWriter;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

import edu.fudan.nlp.tag.CWSTagger;

public class Segment
{
	public static double releaseVersion = 3.0;
	
	public static void main( String[] args )
	{
		GenerateSags();
	}

	public static void GenerateSags()
	{
		System.out.println( "Loading seg.c7.110918.gz ..." );
		CWSTagger tag = null;
		try
		{
			tag = new CWSTagger("./lib/data/seg.c7.110918.gz");
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		Path pp = new Path( releaseVersion, true );
		HDFSFileWriter writer = new HDFSFileWriter( pp.getFilePath( "featureTags" ) );
		
		String[] source = { URICenter.source_name_baidu, URICenter.source_name_hudong, URICenter.source_name_zhwiki };
		for( int i = 0; i <= 2; ++i )
		{
			Path p = new Path( releaseVersion, source[i] );
			
			// TODO: old version
			if( source[i].equals( URICenter.source_name_zhwiki ) )
				p = new Path( 2.0, source[i] );
			
			NTriplesReader reader = new NTriplesReader( p.getFilePath( "disambiguation" ) );
			while( reader.readNextLine() != null )
			{
				TripleReader tr = reader.getTripleReader();
				HashSet<String> features = (new SemanticLabel( tr.getObjectContent(), source[i] )).features;
				if( features.size() > 0 )
				{
					String tags = tag.tag( (String) features.toArray()[0] );
					writer.writeLine( TripleWriter.getTripleLine( tr.getObject(), URICenter.predicate_temp_featureTags, "\""+TextTools.getUnicode(tags)+"\"@zh" ) );
				}
			}
			reader.close();
		}
		writer.close();
	}
}
