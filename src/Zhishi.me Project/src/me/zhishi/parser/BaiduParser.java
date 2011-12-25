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

public class BaiduParser implements Parser
{
	public static void main( String[] args ) throws IOException
	{
		String url = "http://baike.baidu.com/view/277746.htm";
		BaiduParser p = new BaiduParser( url );
		p.parse();
	}
	
	private Document doc;
	
	public BaiduParser( InputStream is ) throws IOException
	{
		doc = Jsoup.parse( is, "GB18030", "http://baike.baidu.com" );
	}

	public BaiduParser( String url ) throws IOException
	{
		doc = Jsoup.connect( url ).get();
	}

	@Override
	public Article parse()
	{
		Article article = new Article( IRICenter.source_name_baidu );
		article.label = getLabel();
		article.categories = getCategories();
		return article;
	}

	@Override
	public String getLabel()
	{
		String label = doc.select("h1[class=title]").html();
		label = StringEscapeUtils.unescapeHtml4(label);
		label = label.trim();
//		System.out.println( label );
//		System.out.println( doc.select("title").text() );
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
		
		for( Element cat : doc.select( "dl#viewExtCati > dd > a" ) )
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
