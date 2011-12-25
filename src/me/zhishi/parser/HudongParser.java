package me.zhishi.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.zhishi.tools.IRICenter;
import me.zhishi.tools.StringPair;

public class HudongParser implements Parser
{
	public static void main(String args[]) throws IOException
	{
		String url = "http://www.hudong.com/wiki/%CE%B2%E8%AE%A1%E7%AE%97%E6%9C%BA";
		HudongParser p = new HudongParser( url );
		p.getLabel();
	}
	
	private Document doc;
	
	public HudongParser( InputStream is ) throws IOException
	{
		doc = Jsoup.parse( is, "UTF-8", "http://www.hudong.com/" );
	}
	
	public HudongParser( String url ) throws IOException
	{
		doc = Jsoup.connect( url ).get();
	}
	
	@Override
	public Article parse()
	{
		Article article = new Article( IRICenter.source_name_hudong );
		article.label = getLabel();
		article.categories = getCategories();
		return article;
	}

	@Override
	public String getLabel()
	{
		String label = doc.select("div[class^=content-h1]").select("h1").html();
		label = StringEscapeUtils.unescapeHtml4(label);
		label = label.trim();
		System.out.println( label );
		System.out.println( doc.select("title").text() );
		return label;
	}

	@Override
	public String getAbstract()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRedirect()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<StringPair> getPictures()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<StringPair> getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getCategories()
	{
		ArrayList<String> categories = new ArrayList<String>();
		
		for( Element cat : doc.select( "div[class=relevantinfo] > dl[id=show_tag] > dd > a" ) )
			if( cat.hasAttr( "href" ) && cat.attr( "href" ).contains( "/categorypage" ) )
				categories.add( cat.text() );
		
		return categories;
	}

	@Override
	public ArrayList<String> getInternalLinks()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getExternalLinks()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getRelatedLabels()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDisambiguationPage()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
