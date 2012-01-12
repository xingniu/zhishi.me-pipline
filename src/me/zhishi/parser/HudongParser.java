package me.zhishi.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.StringPair;

public class HudongParser implements ZhishiParser
{
	public static void main(String args[]) throws IOException
	{
		String url = "http://www.hudong.com/wiki/1月1日";
		HudongParser p = new HudongParser( url );
		Article article = p.parse();
		
		for( String t : article.toTriples() )
		{
			System.out.println( t );
		}
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
		ZhishiArticle article = new ZhishiArticle( URICenter.source_name_hudong );
		article.label = getLabel();
		article.isRedirect = isRedirectPage();
		article.redirect = getRedirect();
		article.isDisambiguationPage = isDisambiguationPage();
		article.disambiguationLabels = getDisambiguations();
		if (article.isRedirect || article.isDisambiguationPage)
			return article;
		
		article.abs = getAbstract();
		article.categories = getCategories();
		article.relatedPages = getRelatedPages();
		article.pictures = getPictures();
		article.properties = getProperties();
		article.internalLinks = getInternalLinks();
		article.externalLinks = getExternalLinks();
		return article;
	}

	@Override
	public String getLabel()
	{
		String label = null;
		if( !isDisambiguationPage() )
		{
			label = doc.select( "div[class^=content-h1]" ).select( "h1" ).text();
		}
		else
		{
			for( Element e : doc.select( "div[class=prompt] > p > a" ) )
				if( e.hasAttr( "href" ) && e.attr( "href" ).contains( "/wiki/" ) )
				{
					label = e.text();
					if( label.contains( "[" ) )
						label = label.substring( 0, label.indexOf( "[" ) );
					break;
				}
		}
		label = StringEscapeUtils.unescapeHtml4(label);
		label = label.trim();
		if( !label.equals( "" ) )
			return label;
		else
			return null;
	}

	@Override
	public String getAbstract()
	{
		String abs = doc.select("div[class=summary] > p").text().replaceAll( whitespace, "" );
		if( abs.startsWith( "请用一段简单的话描述该词条" ) || abs.equals( "" ) )
			return null;
		else
			return abs;
	}

	@Override
	public boolean isRedirectPage(){
		String tmp = doc.select("div[id=unifyprompt] > p[id=unifypromptone]").text();
		return tmp.contains("是") && tmp.substring(tmp.indexOf("是")).contains("的同义词");
	}
	
	@Override
	public String getRedirect()
	{
		if (isRedirectPage()) {
			for (Element re : doc.select("div[id=unifyprompt] > p[id=unifypromptone] > a")){
				String s = re.attr("href");
				if (!s.contains("wiki/"))
					return null;
				String redirect = re.text();
				return redirect;
			}
		}
		return null;
	}

	@Override
	public ArrayList<StringPair> getPictures()
	{
		ArrayList <StringPair> pics = new ArrayList<StringPair>();
		
		for(Element img:doc.select("div[class*=img_r]").select("img"))
			if(img.hasAttr("title")){
				String picTitle = img.attr("title");			
				if (picTitle.startsWith("（图）"))
					picTitle = picTitle.substring(3, picTitle.length());
				if (picTitle.length() == 0)
					picTitle = getLabel();
				picTitle = picTitle.replaceAll(whitespace, "");
				picTitle = picTitle.trim();
				pics.add(new StringPair(img.attr("src"), picTitle));
			}
		for(Element img:doc.select("div[id=docinfotemplettable]").select("img"))
			pics.add(new StringPair(img.attr("src"), getLabel()));
		
		return pics;
	}

	@Override
	public ArrayList<StringPair> getProperties()
	{
		ArrayList <StringPair> properties = new ArrayList<StringPair>();
		
		for (Element infoBox : doc.select("div[id=docinfotemplettable]"))
			for (Element tr : infoBox.select("tr")){
				StringPair p = new StringPair();
				boolean flag1 = false;
				boolean flag2 = false;
				for (Element e : tr.select("td")){
					if (e.hasAttr("align")){
						flag1 = true;
						p.first = e.text();
						if (p.first.contains("："))
							p.first = p.first.substring(0, p.first.indexOf("："));
					}
					if (e.hasAttr("style")){
						flag2 = true;
						p.second = e.text();						
					}
				}
				if (flag1 && flag2) {
					properties.add(p);
				}
			}
		
		return properties;
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
		HashSet<String> internalLinksSet = new HashSet<String>();
		
		for( Element e : doc.select( "a[class=innerlink]" ) )
		{
			if( !e.attr( "title" ).equals( "" ) && !e.text().equals( "" ) && !e.parents().hasClass( "table" ) )
				internalLinksSet.add( e.text() );
		}
		
		return new ArrayList<String>( internalLinksSet );
	}

	@Override
	public ArrayList<String> getExternalLinks()
	{
		ArrayList<String> outerLinks = new ArrayList<String>();

		for( Element link : doc.select( "div[class=relevantinfo] > dl[class^=reference]" ).select( "input" ) )
			if( link.hasAttr( "value" ) )
				outerLinks.add( link.attr( "value" ).replaceAll( "[\\s]", "" ) );
		for( Element link : doc.select( "div[class=relevantinfo] > dl[id=show_quote]" ).select( "a" ) )
			if( link.hasAttr( "href" ) )
			{
				String tmp = link.attr( "href" );
				if( tmp.startsWith( "http://" ) )
					outerLinks.add( tmp.replaceAll( "[\\s]", "" ) );
			}

		return outerLinks;
	}

	@Override
	public ArrayList<String> getRelatedPages()
	{
		ArrayList<String> relatedPages = new ArrayList<String>();
		for( Element relat : doc.select( "div[class^=xgct] > ul" ).select( "a" ) )
			if( relat.hasAttr( "href" ) && relat.attr( "href" ).startsWith( "/wiki/" ) && !relat.attr( "title" ).equals( "" ) )
				relatedPages.add( relat.attr( "title" ) );
		
		return relatedPages;
	}

	@Override
	public boolean isDisambiguationPage()
	{
		return !doc.select("dl[class=polysemy]").isEmpty();
	}
	
	@Override
	public ArrayList<String> getDisambiguations()
	{
		ArrayList<String> disambiguations = new ArrayList<String>();
		
		for (Element e :doc.select("div[class=prompt] > p > a"))
			if (e.hasAttr("href") && e.attr("href").contains("/wiki/"))
				disambiguations.add( e.text());

		return disambiguations;
	}

}
