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

public class BaiduParser implements ZhishiParser
{
	public static void main( String[] args ) throws IOException
	{
		String url = "http://baike.baidu.com/view/10863.htm";
		BaiduParser p = new BaiduParser( url );
		Article article = p.parse();
		
		for( String t : article.toTriples() )
		{
			System.out.println( t );
		}
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
		ZhishiArticle article = new ZhishiArticle( URICenter.source_name_baidu );
		article.label = getLabel();
		article.isRedirect = isRedirectPage(); 
		article.redirect = getRedirect();
		article.isDisambiguationPage = isDisambiguationPage();
		article.disambiguationLabels = getDisambiguations();
		if (article.isDisambiguationPage)
		{
			article.disambiguationArticles = BaiduDisParse();
		}
		if (article.isRedirect || article.isDisambiguationPage) {
			return article;
		}
		
		article.abs = getAbstract();
		article.categories = getCategories();
		article.relatedPages = getRelatedLabels();
		article.pictures = getPictures();
		article.properties = getProperties();
		article.internalLinks = getInternalLinks();
		article.externalLinks = getExternalLinks();
			
		return article;
	}

	@Override
	public String getLabel()
	{
		if( doc.select( "h1[class=title]" ).hasText() )
		{
			// .ownText(): see http://baike.baidu.com/view/4067040.htm
			String label = doc.select( "h1[class=title]" ).first().ownText();
			label = StringEscapeUtils.unescapeHtml4( label );
			label = label.trim();
			if( !label.equals( "" ) )
				return label;
		}
		return null;
	}

	@Override
	public String getAbstract()
	{
		String abs = doc.select( "div[class*=card-summary] > p" ).text().replace( whitespace, "" );
		if( abs.equals( "" ) )
			return null;
		else
			return abs;
	}
	
	@Override
	public boolean isRedirectPage(){
		return getRedirect() != null;
	}
	
	@Override
	public String getRedirect()
	{
		String redirect = null;
		if (!doc.select("div[class^=view-tip-pannel]").select("a[href$=redirect]").isEmpty())
			redirect = doc.select("div[class^=view-tip-pannel]").select("a[href$=redirect]").text();

		if (!doc.select("div[class^=view-tip-pannel]").select("a[class$=synstd]").isEmpty()) 
			redirect = doc.select("div[class^=view-tip-pannel]").select("a[href*=history]").text();
		
		if (redirect!=null){
			if (redirect.contains(getLabel()) && isDisambiguationPage()) 
				return null;
			redirect = StringEscapeUtils.unescapeHtml4(redirect);
			if (redirect.length() > 80) return "";
		}
		return redirect;
	}

	@Override
	public ArrayList<StringPair> getPictures()
	{
		ArrayList <StringPair> pics = new ArrayList<StringPair>();
		
		for(Element img:doc.select("div[class*=main-body]").select("img"))
			if(img.hasAttr("title")) {
				String picTitle = img.attr("title");
				if (picTitle.length() == 0)
					picTitle = getLabel();
				picTitle = picTitle.replaceAll(whitespace, "");
				picTitle = picTitle.trim();
				pics.add(new StringPair(img.attr("src"), picTitle));
			}
		
		return pics;
	}

	@Override
	public ArrayList<StringPair> getProperties()
	{
		ArrayList <StringPair> properties = new ArrayList<StringPair>();
		
		for(Element e:doc.select("div[class*=card-info] td[class=cardFirstTd")){
			StringPair p = new StringPair();
			p.first = e.text();
			if (p.first.contains("："))
				p.first = p.first.substring(0, p.first.indexOf("："));
			properties.add(p);
		}
		int i = 0;
		for(Element e:doc.select("div[class*=card-info] td[class=cardSecondTd")){
			properties.get(i).second = e.text();
			++i;
		}
		
		return properties;
	}

	@Override
	public ArrayList<String> getCategories()
	{
		ArrayList<String> categories = new ArrayList<String>();
		
		for( Element cat : doc.select( "dl#viewExtCati > dd > a" ) )
		{
			if( !cat.text().equals( "" ) )
				categories.add( cat.text() );
		}
		
		return categories;
	}

	@Override
	public ArrayList<String> getInternalLinks()
	{
		ArrayList<String> innerLinks = new ArrayList<String>();
		
		for (Element link: doc.select("a[href^=/view/] , a[href^=http://baike.baidu.com/view/"))
			if(link.hasAttr("href") && link.attr("href").endsWith("htm")) {
				if (link.parent().hasAttr("class") && link.parent().attr("class").endsWith("cardSecondTd")) {
				} else {
					innerLinks.add( StringEscapeUtils.unescapeHtml4(link.text()));
				}
			}
		
		return innerLinks;
	}

	@Override
	public ArrayList<String> getExternalLinks()
	{
		ArrayList<String> outerLinks = new ArrayList<String>();
		
		for (Element link: doc.select("div[class*=main-body]").select("a"))
			if (link.hasAttr("href")){
				String tmp = link.attr("href");
				if (tmp.startsWith("http://") && !tmp.startsWith("http://baike.baidu.com/view/"))
					outerLinks.add(tmp.replaceAll( "[\\s]", "" ));
			}
		
		return outerLinks;
	}

	@Override
	public ArrayList<String> getRelatedLabels()
	{
		ArrayList<String> relatedLabels = new ArrayList<String>();
		for ( Element relat: doc.select( "dl#relatedLemmaDown > dd" ).select("a") )
			if ( relat.hasAttr("href") && relat.attr("href").startsWith("/view/") ) 
				relatedLabels.add( StringEscapeUtils.unescapeHtml4(relat.text()) );
		
		return relatedLabels;	
	}

	@Override
	public boolean isDisambiguationPage()
	{
		return !doc.select("div[class*=nslog:517]").isEmpty();
	}
	
	@Override
	public ArrayList<String> getDisambiguations()
	{
		ArrayList<String> disambiguations = new ArrayList<String>();
		
		for (Element link :doc.select("ol[data-nslog-type=503] > li > a")) {
			String tmp = getLabel() + "[" + link.text() + "]";
			disambiguations.add(tmp);	
		}

		return disambiguations;
	}
	
	public ArrayList<Article> BaiduDisParse() 
	{
		ArrayList<Article> disArticles = new ArrayList<Article>();

		ArrayList<String> disList = getDisambiguations();
		ArrayList <String> list = new ArrayList<String>();
		for (Element e: doc.select("div[class*=polysemy-item-cnt]"))
			list.add(e.outerHtml());
		
		for (int i = 0; i < disList.size(); ++i) {
			ZhishiArticle article = new ZhishiArticle( URICenter.source_name_baidu );
			doc = Jsoup.parse(list.get(i));
			
			article.label = disList.get(i);
			article.categories = getCategories();
			article.abs = getAbstract();
			article.isRedirect = isDisRedirectPage();
			if (article.isRedirect) {
				article.redirect = article.label;
				article.label = article.label.substring(article.label.indexOf("[") + 1, article.label.indexOf("]"));
			}
			article.relatedPages = getRelatedLabels();
			article.pictures = getDisPictures(article.label);
			article.properties = getProperties();
			article.internalLinks = getInternalLinks();
			article.externalLinks = getDisExternalLinks();
			article.isDisambiguationPage = false;
			disArticles.add(article);
		}
		
		return disArticles;
	}
	
	public boolean isDisRedirectPage() {
		
		if (!doc.select("div[class^=view-tip-pannel]").select("a[href$=redirect]").isEmpty())
			return true;
		if (!doc.select("div[class^=view-tip-pannel]").select("a[class$=synstd]").isEmpty()) 
			return true;

		return false;
	}
	
	public ArrayList<StringPair> getDisPictures(String DisLabel) {
		ArrayList <StringPair> pics = new ArrayList<StringPair>();
		for(Element img :doc.select("img"))
			if(img.hasAttr("title")) {
				String picTitle = img.attr("title");
				if (picTitle.length() == 0)
					picTitle = DisLabel;
				picTitle = picTitle.replaceAll(whitespace, "");
				picTitle = picTitle.trim();
				pics.add(new StringPair(img.attr("src"), picTitle));
			}
		return pics;
	}
	
	
	public ArrayList<String> getDisExternalLinks() {
		ArrayList<String> outerLinks = new ArrayList<String>();
		for (Element link: doc.select("a")){
			if (link.hasAttr("href")){
				String tmp = link.attr("href");
				if (tmp.startsWith("http://") && !tmp.startsWith("http://baike.baidu.com/view/"))
					outerLinks.add(tmp);
			}
		}
		return outerLinks;
	}
}
