package me.zhishi.crawler;

import java.io.IOException;
import java.util.HashSet;

import me.zhishi.analyzer.DataAnalyzer;
import me.zhishi.tools.Path;
import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.GZIPFileWriter;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CategoryCrawler
{
	public static double releaseVersion = 3.2;
	private static String UserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.1 (KHTML, like Gecko) Maxthon/4.0.5.4000 Chrome/26.0.1410.43 Safari/537.1";
	
	private static HashSet<String> categorySet;
	private static int counter;
	private static GZIPFileWriter writer;
	
	public static void main( String[] args )
	{
		//storeHudongSKOS();
		storeBaiduSKOS();
	}
	
	public static void storeBaiduSKOS()
	{
		categorySet = new HashSet<String>();
		counter = 0;
		Path p = new Path( releaseVersion, URICenter.source_name_baidu );
		writer = new GZIPFileWriter( p.getNTriplesFile( "skosCat" ) );
		
		DataAnalyzer da = new DataAnalyzer( releaseVersion, URICenter.source_name_baidu, "category" );
		while( da.fileReader.readNextLine() != null )
		{
			TripleReader tr = da.fileReader.getTripleReader();
			String category = tr.getObjectContent();
			categorySet.add( category ); 
		}
		da.closeReader();
		int size = categorySet.size();
		
		for( String category : categorySet )
		{
			try
			{
				System.err.println( ++counter + "/" + size + " : " + category );
				getBaiduSubCategory( category );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		
		writer.close();
	}
	
	public static void getBaiduSubCategory( String node ) throws InterruptedException
	{
		Document doc = null;
		String url = "http://baike.baidu.com/fenlei/" + TextTools.encoder( node );
		
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
		
		URICenter uc = new URICenter( URICenter.source_name_baidu );
		
		if( !doc.select( "div[class=g-row bread log-set-param]" ).isEmpty() )
		{
			writer.writeLine( TripleWriter.getStringValueTriple( uc.getCategoryURI( node ), URICenter.predicate_skos_prefLabel, node ) );
			writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( node ), URICenter.predicate_rdf_type, URICenter.class_skos_concept ) );
			
			boolean notoutput = true;
			for( Element e : doc.select( "div[class=g-row bread log-set-param] > div > a" ) )
			{
				if( notoutput )
				{
					notoutput = false;
					continue;
				}
				System.out.println( node + " <<< " + e.text() );
				writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( node ), URICenter.predicate_skos_broader, uc.getCategoryURI( e.text() ) ) );
				writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( e.text() ), URICenter.predicate_skos_narrower, uc.getCategoryURI( node ) ) );
			}
		}
	}
	
	public static void storeHudongSKOS()
	{
		categorySet = new HashSet<String>();
		counter = 0;
		Path p = new Path( releaseVersion, URICenter.source_name_hudong );
		writer = new GZIPFileWriter( p.getNTriplesFile( "skosCat" ) );
		
		String root = "页面总分类";
		try
		{
			getHudongSubCategory( root, "1", 0 );
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
	
	public static void getHudongSubCategory( String node, String info, int depth ) throws InterruptedException, JSONException
	{
		Document doc = null;
		String url = "http://www.baike.com/category/Ajax_cate.jsp?catename=" + TextTools.encoder( node );
		
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
			
			writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( node ), URICenter.predicate_skos_narrower, uc.getCategoryURI( cNode ) ) );
			writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( cNode ), URICenter.predicate_skos_broader, uc.getCategoryURI( node ) ) );
			
			counter++;
			if( categorySet.contains( cNode ) )
				continue;
			categorySet.add( cNode );
			System.err.println( categorySet.size() + "/" + counter );
			getHudongSubCategory( cNode, info + '-' + (i+1), depth + 1 );
		}
	}
}
