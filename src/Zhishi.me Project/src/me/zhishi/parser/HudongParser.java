package me.zhishi.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
		String url = "http://www.hudong.com/wiki/%E4%B8%8A%E6%B5%B7";
		HudongParser p = new HudongParser( url );
		p.parse();
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
		article.abs = getAbstract();
		article.redirect = getRedirect();
		article.pictures = getPictures();
		article.categories = getCategories();
		article.isRedirect = isRedirectPage(); 
		article.relatedPages = getRelatedLabels();
		article.properties = getProperties();
		article.internalLinks = getInternalLinks();
		article.externalLinks = getExternalLinks();
		article.isDisambiguationPage = isDisambiguationPage();
		return article;
	}

	@Override
	public String getLabel()
	{
		String label = doc.select("div[class^=content-h1]").select("h1").html();
		label = StringEscapeUtils.unescapeHtml4(label);
		label = label.trim();
//		System.out.println( label );
//		System.out.println( doc.select("title").text() );
		return label;
	}

	@Override
	public String getAbstract()
	{
		String abs = doc.select("div[class=summary]").select("p").text();
		return abs.replaceAll(whitespace, "");
	}

	@Override
	public boolean isRedirectPage(){
		String tmp = doc.select("div[id=unifyprompt] > p[id=unifypromptone]").text();
		return tmp.contains("是") && tmp.substring(tmp.indexOf("是")).contains("的同义词");
	}
	
	@Override
	public String getRedirect()
	{
		int count = 0;
		for (Element re : doc.select("div[id=unifyprompt] > p[id=unifypromptone] > a")){
			count++;
			if (count == 2){
				String redirect = re.attr("href");
				if (!redirect.contains("wiki/"))
					return null;
				redirect = redirect.substring(redirect.indexOf("wiki/")+5, redirect.length());
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
		ArrayList<String> innerLinks = new ArrayList<String>();
		
		for (Element e: doc.select("a[class=innerlink]")) 
			if (!e.parents().hasClass("table")) 
				innerLinks .add( e.text() );
		
		return innerLinks;
	}

	@Override
	public ArrayList<String> getExternalLinks()
	{
		ArrayList<String> outerLinks = new ArrayList<String>();
		
		for (Element link : doc.select("div[class=relevantinfo] > dl[class^=reference]").select("input"))
			if (link.hasAttr("value"))
				outerLinks.add(link.attr("value"));
		for (Element link : doc.select("div[class=relevantinfo] > dl[id=show_quote]").select("a"))
			if (link.hasAttr("href")){
				String tmp = link.attr("href");
				if (tmp.startsWith("http://"))
					outerLinks.add(tmp);
			}

		return outerLinks;
	}

	@Override
	public ArrayList<String> getRelatedLabels()
	{
		ArrayList<String> relatedLabels = new ArrayList<String>();
		for (Element relat : doc.select( "div[class^=xgct] > ul" ).select("a") )
			if ( relat.hasAttr( "href" ) && relat.attr( "href" ).startsWith("/wiki/") )
				relatedLabels.add( relat.attr("title") );
		
		return relatedLabels;
	}

	@Override
	public boolean isDisambiguationPage()
	{
		return !doc.select("dl[class=polysemy]").isEmpty();
	}

}
