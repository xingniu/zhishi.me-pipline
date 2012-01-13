package me.zhishi.parser.tools;

import java.io.IOException;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.zhishi.tools.Path;
import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleWriter;
import me.zhishi.tools.file.ZIPFileWriter;

public class CategoryStorer
{
	public static double releaseVersion = 3.0;
	
	public static void main(String[] args)
	{
		storeHudongSKOS();
	}
	
	private static HashSet<String> categorySet;
	private static int counter;
	private static ZIPFileWriter writer;
	
	public static void storeHudongSKOS()
	{
		categorySet = new HashSet<String>();
		counter = 0;
		Path p = new Path( releaseVersion, URICenter.source_name_hudong );
		writer = new ZIPFileWriter( p.getNTriplesPath(), p.getFileName( "skosCat" ) );
		
		String root = TextTools.encoder( "页面总分类" );
		try
		{
			getSubCategory( root, "1", 0 );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		
		writer.close();
	}
	
	public static void getSubCategory( String node, String info, int depth ) throws InterruptedException
	{
		Document doc = null;
		String url = "http://www.hudong.com/categorypage/show/" + node + "/";
		String deNode = TextTools.decoder( node );
		
		for( int i = 0; i <= 100; ++i )
		{
			try
			{
				doc = Jsoup.connect( url ).get();
				break;
			}
			catch( IOException e )
			{
				System.err.println( "IOException, try again. " + deNode );
				Thread.sleep( 10000 );
			}
		}
		
		URICenter uc = new URICenter( URICenter.source_name_hudong );
		
		writer.writeLine( TripleWriter.getStringValueTriple( uc.getCategoryURI( deNode ), URICenter.predicate_skos_prefLabel, deNode ) );
		writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( deNode ), URICenter.predicate_rdf_type, URICenter.class_skos_concept ) );
		
		for( Element fe : doc.select( "div[class=left_c]" ) )
		{
			if( fe.select( "h2" ).text().equals( "子分类" ) )
			{
				int i = 1;
				for( Element ce : fe.select( "ul > li > a" ) )
				{
					String cNode = ce.attr( "href" ).replaceAll( "http.*show/", "" ).replaceAll( "/\\?prd.*", "" );
					String deCNode = TextTools.decoder( cNode );
					System.out.println( depth + " " + info + " " + deNode + " >>> " + deCNode );
					
					writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( deNode ), URICenter.predicate_skos_broader, uc.getCategoryURI( deCNode ) ) );
					writer.writeLine( TripleWriter.getResourceObjectTriple( uc.getCategoryURI( deCNode ), URICenter.predicate_skos_narrower, uc.getCategoryURI( deNode ) ) );
					
					counter++;
					if( categorySet.contains( cNode ) )
						continue;
					categorySet.add( cNode );
					System.err.println( categorySet.size() + "/" + counter );
					getSubCategory( cNode, info + '-' + i, depth + 1 );
					i++;
				}
			}
		}
	}
}
