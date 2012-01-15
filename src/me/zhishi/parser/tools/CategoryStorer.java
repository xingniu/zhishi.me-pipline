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
import me.zhishi.tools.file.NTriplesReader;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;
import me.zhishi.tools.file.ZIPFileWriter;

public class CategoryStorer
{
	public static double releaseVersion = 3.0;
	public static String source = URICenter.source_name_baidu;
//	public static String source = URICenter.source_name_hudong;
	
	private static ZIPFileWriter writer;
	
	public static void main(String[] args)
	{
//		storeHudongSKOS();
		storeCategoryLabels();
	}
	
	public static void storeCategoryLabels()
	{
		Path p = new Path( releaseVersion, source );
		writer = new ZIPFileWriter( p.getNTriplesPath(), p.getFileName( "categoryLabel" ) );
		NTriplesReader reader = new NTriplesReader( p.getFilePath( "category" ) );
		
		HashSet<String> categoriesSet = new HashSet<String>();
		while( reader.readNextLine() != null )
		{
			TripleReader tr = reader.getTripleReader();
			String category = tr.getObjectContent();
			categoriesSet.add( category );
		}
		
		URICenter uc = new URICenter( source );
		for( String c : categoriesSet )
		{
			writer.writeLine( TripleWriter.getStringValueTriple( uc.getCategoryURI( c ), URICenter.predicate_rdfs_label, c ) );
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
		writer = new ZIPFileWriter( p.getNTriplesPath(), p.getFileName( "skosCat" ) );
		
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
}
