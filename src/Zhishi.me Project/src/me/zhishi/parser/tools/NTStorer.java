package me.zhishi.parser.tools;

import java.io.IOException;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.zhishi.tools.Path;
import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.GZIPFileWriter;
import me.zhishi.tools.file.HDFSFileReader;
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

public class NTStorer
{
	public static double releaseVersion = 3.0;
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	public static String[] contents = {
		"label",
		"category",
		"abstract",
		"externalLink",
		"relatedPage",
		"internalLink",
		"redirect",
		"disambiguation",
		"articleLink",
		"image",
		"imageInfo",
		"infobox",
		};
	
	private static GZIPFileWriter writer;
	
	public static void main(String[] args)
	{
		storeHDFSFile();
		// TODO: version 2.9
//		store( 2.9, URICenter.source_name_zhishi, "lookup" );
//		storeMatches();
//		storeLabels( "category", "categoryLabel" );
//		storeLabels( "infobox", "propertyLabel" );
//		storeOntologyDefinition();
//		storeHudongSKOS();
	}
	
	public static void storeLabels( String inKey, String outKey )
	{
		Path p = new Path( releaseVersion, source );
		writer = new GZIPFileWriter( p.getNTriplesFile( outKey ) );
		NTriplesReader reader = new NTriplesReader( p.getNTriplesFile( inKey ) );
		
		HashSet<String> labelSet = new HashSet<String>();
		while( reader.readNextLine() != null )
		{
			TripleReader tr = reader.getTripleReader();
			String label = null;
			if( inKey.equals( "category" ) )
				label = tr.getObjectContent();
			else if( inKey.equals( "infobox" ) )
				label = tr.getPredicateContent();
			labelSet.add( label );
		}
		
		URICenter uc = new URICenter( source );
		for( String c : labelSet )
		{
			if( inKey.equals( "category" ) )
				writer.writeLine( TripleWriter.getStringValueTriple( uc.getCategoryURI( c ), URICenter.predicate_rdfs_label, c ) );
			else if( inKey.equals( "infobox" ) )
				writer.writeLine( TripleWriter.getStringValueTriple( uc.getPropertyPredicate( c ), URICenter.predicate_rdfs_label, c ) );
		}
		
		reader.close();
		writer.close();
	}
	
	private static HashSet<String> categorySet;
	private static int counter;

	public static void storeHudongSKOS()
	{
		categorySet = new HashSet<String>();
		counter = 0;
		Path p = new Path( releaseVersion, URICenter.source_name_hudong );
		writer = new GZIPFileWriter( p.getNTriplesFile( "skosCat" ) );
		
		String root = "页面总分类";
		try
		{
			getSubCategory( root, "1", 0 );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		
		writer.close();
	}
	
	public static void getSubCategory( String node, String info, int depth ) throws InterruptedException, JSONException
	{
		Document doc = null;
		String UserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.3 (KHTML, like Gecko) Maxthon/3.3.3.1000 Chrome/16.0.883.0 Safari/535.3";
		String url = "http://www.hudong.com/category/Ajax_cate.jsp?catename=" + TextTools.encoder( node );
		
		for( int i = 0; i <= 100; ++i )
		{
			try
			{
				doc = Jsoup.connect( url ).userAgent( UserAgent ).get();
				break;
			}
			catch( IOException e )
			{
				System.err.println( "IOException, try again. " + node );
				Thread.sleep( 10000 * i );
			}
		}
		
		URICenter uc = new URICenter( URICenter.source_name_hudong );
		
		writer.writeLine( TripleWriter.getStringValueTriple( uc.getCategoryURI( node ), URICenter.predicate_skos_prefLabel, node ) );
		writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( node ), URICenter.predicate_rdf_type, URICenter.class_skos_concept ) );
		
		JSONArray array = null;
		try
		{
			array = new JSONArray( doc.text() );
		}
		catch( JSONException e )
		{
			return;
		}
		
		for( int i = 0; i < array.length(); ++i )
		{
			String cNode = ((JSONObject) array.get( i )).getString( "name" );
			System.out.println( depth + " " + info + " " +node + " >>> " + cNode );
			
			writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( node ), URICenter.predicate_skos_broader, uc.getCategoryURI( cNode ) ) );
			writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( cNode ), URICenter.predicate_skos_narrower, uc.getCategoryURI( node ) ) );
			
			counter++;
			if( categorySet.contains( cNode ) )
				continue;
			categorySet.add( cNode );
			System.err.println( categorySet.size() + "/" + counter );
			getSubCategory( cNode, info + '-' + (i+1), depth + 1 );
		}
	}
	
	public static void storeHDFSFile()
	{
		for( String c : contents )
		{
			store( releaseVersion, source, c );
		}
	}
	
	public static void storeMatches()
	{
		// TODO: version 2.9
		Path pp = new Path( 2.9 );
		pp.setSource( URICenter.source_name_baidu );
		GZIPFileWriter writer1 = new GZIPFileWriter( pp.getNTriplesFile( "hudongLink" ) );
		GZIPFileWriter writer2 = new GZIPFileWriter( pp.getNTriplesFile( "zhwikiLink" ) );
		pp.setSource( URICenter.source_name_hudong );
		GZIPFileWriter writer3 = new GZIPFileWriter( pp.getNTriplesFile( "zhwikiLink" ) );
		GZIPFileWriter writer4 = new GZIPFileWriter( pp.getNTriplesFile( "baiduLink" ) );
		pp.setSource( URICenter.source_name_zhwiki );
		// TODO: dump version 
		pp.setDumpVersion( "2011" );
		GZIPFileWriter writer5 = new GZIPFileWriter( pp.getNTriplesFile( "baiduLink" ) );
		GZIPFileWriter writer6 = new GZIPFileWriter( pp.getNTriplesFile( "hudongLink" ) );
		
		Path hp = new Path( releaseVersion, true );
		HDFSFileReader reader = new HDFSFileReader( hp.getMatchingFile() );
		
		String[] ns = { URICenter.namespace_baidu, URICenter.namespace_hudong, URICenter.namespace_zhwiki };
//		HashSet<String> instanceSet = new HashSet<String>();
		int count[] = new int[3];
		String line = null;
		while( (line = reader.readLine()) != null )
		{
			String segs[] = line.split( "\t" );
			if( segs[1].contains( ns[0] ) )
			{
				count[0]++;
				writer1.writeLine( TripleWriter.getTripleLine( segs[1], URICenter.predicate_owl_sameAs, segs[2] ) );
				writer4.writeLine( TripleWriter.getTripleLine( segs[2], URICenter.predicate_owl_sameAs, segs[1] ) );
			}
			else if( segs[1].contains( ns[1] ) )
			{
				count[1]++;
				writer3.writeLine( TripleWriter.getTripleLine( segs[1], URICenter.predicate_owl_sameAs, segs[2] ) );
				writer6.writeLine( TripleWriter.getTripleLine( segs[2], URICenter.predicate_owl_sameAs, segs[1] ) );
//				if( instanceSet.contains( segs[2] ) )
//					count++;
//				else
//					instanceSet.add( segs[2] );
			}
			else if( segs[1].contains( ns[2] ) )
			{
				count[2]++;
				writer5.writeLine( TripleWriter.getTripleLine( segs[1], URICenter.predicate_owl_sameAs, segs[2] ) );
				writer2.writeLine( TripleWriter.getTripleLine( segs[2], URICenter.predicate_owl_sameAs, segs[1] ) );
//				if( instanceSet.contains( segs[1] ) )
//				count++;
//			else
//				instanceSet.add( segs[1] );
			}
		}
		reader.close();

		System.out.println( count[0] );
		System.out.println( count[1] );
		System.out.println( count[2] );
		
		writer1.close();
		writer2.close();
		writer3.close();
		writer4.close();
		writer5.close();
		writer6.close();
	}
	
	public static void storeOntologyDefinition()
	{
		Path p = new Path( releaseVersion );
		GZIPFileWriter writer = new GZIPFileWriter( p.getNTriplesFolder(), p.getNTriplesFileName( "ontology" ) );
		
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_abstract, URICenter.predicate_rdf_type, URICenter.object_owl_DatatypeProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_abstract, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_abstract, URICenter.predicate_rdfs_label, "has abstract", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_abstract, URICenter.predicate_rdfs_label, "摘要", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_abstract, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_abstract, URICenter.predicate_rdfs_range, URICenter.object_xmls_string ) );
		
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_disambiguation, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_disambiguation, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_disambiguation, URICenter.predicate_rdfs_label, "encyclopedia page disambiguates", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_disambiguation, URICenter.predicate_rdfs_label, "消歧义", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_disambiguation, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_disambiguation, URICenter.predicate_rdfs_range, URICenter.object_owl_Thing ) );

		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_externalLink, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_externalLink, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_externalLink, URICenter.predicate_rdfs_label, "link from an encyclopedia page to an external page", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_externalLink, URICenter.predicate_rdfs_label, "外部链接", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_externalLink, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_externalLink, URICenter.predicate_rdfs_range, URICenter.object_foaf_Document ) );
		
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdfs_label, "thumbnail of the resource' depiction", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdfs_label, "缩略描述图片", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_depictionThumbnail, URICenter.predicate_rdfs_range, URICenter.object_foaf_Image ) );

		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_internalLink, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_internalLink, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_internalLink, URICenter.predicate_rdfs_label, "link from an encyclopedia page to another encyclopedia page", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_internalLink, URICenter.predicate_rdfs_label, "百科内链", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_internalLink, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_internalLink, URICenter.predicate_rdfs_range, URICenter.object_owl_Thing ) );

		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_redirect, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_redirect, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_redirect, URICenter.predicate_rdfs_label, "encyclopedia page redirect", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_redirect, URICenter.predicate_rdfs_label, "重定向", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_redirect, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_redirect, URICenter.predicate_rdfs_range, URICenter.object_owl_Thing ) );
		
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedPage, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedPage, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_relatedPage, URICenter.predicate_rdfs_label, "related encyclopedia page", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_relatedPage, URICenter.predicate_rdfs_label, "相关词条", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedPage, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedPage, URICenter.predicate_rdfs_range, URICenter.object_owl_Thing ) );

		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedImage, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedImage, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_relatedImage, URICenter.predicate_rdfs_label, "related image", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_relatedImage, URICenter.predicate_rdfs_label, "相关图片", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedImage, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_relatedImage, URICenter.predicate_rdfs_range, URICenter.object_foaf_Image ) );

		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_category, URICenter.predicate_rdf_type, URICenter.object_owl_ObjectProperty ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_category, URICenter.predicate_rdfs_isDefinedBy, URICenter.object_zhishi ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_category, URICenter.predicate_rdfs_label, "category", "en" ) );
		writer.writeLine( TripleWriter.getStringValueTripleAT( URICenter.predicate_category, URICenter.predicate_rdfs_label, "开放分类", "zh" ) );
		writer.writeLine( TripleWriter.getTripleLine( URICenter.predicate_category, URICenter.predicate_rdfs_domain, URICenter.object_owl_Thing ) );
		
		writer.close();
	}
	
	private static void store( double releaseVer, String src, String content )
	{
		Path hp = new Path( releaseVer, src, true );
		HDFSFileReader hReader = new HDFSFileReader( hp.getNTriplesFile( content ) );
		Path pp = new Path( releaseVer, src, false );
		GZIPFileWriter zWriter = new GZIPFileWriter( pp.getNTriplesFile( content ) );

		System.out.println( "Copying " + pp.getNTriplesFileName( content ) );

		String line = null;
		while( (line = hReader.readLine()) != null )
		{
			zWriter.writeLine( line );
		}
		hReader.close();
		zWriter.close();
	}
}
