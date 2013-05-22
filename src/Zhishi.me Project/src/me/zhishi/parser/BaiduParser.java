package me.zhishi.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.zhishi.parser.ZhishiArticle.ImageInfo;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.StringPair;

public class BaiduParser implements ZhishiParser
{
	private static String base = "http://baike.baidu.com/";
	
	public static void main( String[] args ) throws Exception
	{
		String fileName = "1000173.htm";
		String url = base + "view/" + fileName;
		BaiduParser p = new BaiduParser( url, fileName );
		Article article = p.parse();
		
		for( String t : article.toTriples() )
		{
			System.out.println( t );
		}
	}
	
	protected Document doc;
	private String fileName;
	
	public BaiduParser( InputStream is, String fileName ) throws IOException
	{
		doc = Jsoup.parse( is, "GB18030", base );
		this.fileName = fileName;
	}

	public BaiduParser( String url, String fileName ) throws IOException
	{
		doc = Jsoup.connect( url ).get();
		this.fileName = fileName;
	}

	@Override
	public Article parse() throws Exception
	{
		ZhishiArticle article = new ZhishiArticle( URICenter.source_name_baidu );
		
		if( !doc.select( "meta[http-equiv=Refresh]" ).isEmpty() )
		{
			String URL = doc.select( "meta[http-equiv=Refresh]" ).attr( "content" );
			if( URL.contains( "/view/" ) )
				throw new Exception( base + URL.substring( URL.indexOf( "URL=" ) + 5 ) );
			return article;
		}
		if( doc.select( "title" ).text().contains( "操作失败提示" ) )
			return article;
		if( !doc.select( "title" ).text().contains( "百度百科" ) )
			throw new Exception( "Bad Encoding" );
		
		article.articleLink = base + "view/" + fileName;
		article.isRedirect = isRedirectPage();
		if( article.isRedirect )
		{
			article.label = getRedirect();
			article.redirect = getLabel();
			if( isDisambiguationPage() )
			{
				// see http://baike.baidu.com/view/1587783.htm
				for( Element link : doc.select( "ol[data-nslog-type=503] > li[class=expand] > a" ) )
				{
					article.redirect = article.redirect + "[" + link.text() + "]";
				}
			}
			return article;
		}
		else
		{
			article.label = getLabel();
			article.redirect = getRedirect();
		}
		
		article.isDisambiguationPage = isDisambiguationPage();
		article.disambiguationLabels = getDisambiguations();
		if( article.isDisambiguationPage )
		{
			article.disambiguationArticles = BaiduDisParse();
			return article;
		}
		
		article.abs = getAbstract();
		article.categories = getCategories();
		article.relatedPages = getRelatedPages();
		article.imageInfo = getImageInfo();
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
		String abs = doc.select( "div[class*=card-summary] > div" ).text().replace( whitespace, "" );
		if( abs.equals( "" ) )
			return null;
		else
			return abs;
	}
	
	@Override
	public boolean isRedirectPage()
	{
		return getRedirect() != null;
	}
	
	@Override
	public String getRedirect()
	{
		String redirect = null;
		Element element = doc.body();
		
		if( fileName.contains( "subLemmaId" ) )
		{
			String subLemmaId = fileName.substring( fileName.indexOf( "subLemmaId" ) + 11 );
			if( subLemmaId.contains( "&" ) )
				subLemmaId = subLemmaId.substring( 0, subLemmaId.indexOf( "&" ) );
			element = element.select( "div[sublemmaid=" + subLemmaId + "]" ).first();
		}
		
		if( !element.select( "div[class^=view-tip-pannel] > p > a[class$=redirect]" ).isEmpty() )
			redirect = element.select( "div[class^=view-tip-pannel]" ).select( "a[href$=hold=syn]" ).text();
			// 1366565.htm?fromId=6228850
		else if( !element.select( "div[class^=view-tip-pannel] > p > a[class$=synstd]" ).isEmpty() )
			redirect = element.select( "div[class^=view-tip-pannel]" ).select( "a[href^=/history/]" ).text();
			// 1735.htm?fromId=2539
		
		if( redirect != null )
		{
			redirect = StringEscapeUtils.unescapeHtml4( redirect );
			if( redirect.equals( "" ) )
				return null;
		}
		return redirect;
	}
	
	private String getCompleteImg( String img )
	{
		return img.replaceAll( "/abpic/", "/pic/" );
	}
	
	private String getCleanImgTitle( String title )
	{
		if( title.equals( "null" ) )
			return "";
		return title;
	}
	
	@Override
	public ImageInfo getImageInfo()
	{
		String prefix = "http://baike.baidu.com";
		ImageInfo imageInfo = new ImageInfo();
		
		for( Element pic : doc.select( "div[class^=card-summary] > div[class=pic] > a" ) )
		{
			if( pic.attr( "class" ).equals( "card-pic-handle" ) )
				break;
			for( Element img : pic.getElementsByAttributeValue( "class", "card-image editorImg log-set-param" ) )
			{
				String imgURI = getValidURL( img.attr( "src" ) );
				if( imgURI == null )
					continue;
				imageInfo.depictionThumbnail = imgURI;
				imageInfo.depiction = getCompleteImg( imageInfo.depictionThumbnail );
				imageInfo.labels.add( new StringPair( imageInfo.depiction, getCleanImgTitle( img.attr( "title" ) ) ) );
				imageInfo.rights.add( new StringPair( imageInfo.depiction, getValidURL( prefix + pic.attr( "href" ) ) ) );
				imageInfo.thumbnails.add( new StringPair( imageInfo.depiction, imageInfo.depictionThumbnail ) );
			}
		}
		
		//由于百度百科图片的跨站保护，正文图片信息暂停抽取
		
		return imageInfo;
	}

	@Override
	public ArrayList<StringPair> getProperties()
	{
		ArrayList <StringPair> properties = new ArrayList<StringPair>();
		
		for( Element e : doc.select( "div[class=card-info-inner]" ) )
		{
			for( Element info : e.select( "td[class=cardFirstTd]" ) )
			{
				String property = info.text();
				property = property.replaceAll( "[：:].*", "" );
				property = property.replaceAll( "[　 ]", "" );
				properties.add( new StringPair( property, null ) );
			}
			int i = 0;
			for( Element info : e.select( "td[class=cardSecondTd]" ) )
			{
				properties.get( i ).second = info.text();
				++i;
			}
		}
		
		return properties;
	}

	@Override
	public ArrayList<String> getCategories()
	{
		ArrayList<String> categories = new ArrayList<String>();
		
		for( Element cat : doc.select( "dl#viewExtCati > dd > a[href^=/fenlei/]" ) )
		{
			if( !cat.text().equals( "" ) )
				categories.add( cat.text() );
		}
		
		return categories;
	}

	@Override
	public ArrayList<String> getInternalLinks()
	{
		HashSet<String> internalLinksSet = new HashSet<String>();
		
		for( Element link : doc.select( "div[class^=lemma-main-content] a[href^=/view/]" ) )
		{
			if( link.hasAttr( "href" ) && link.attr( "href" ).endsWith( "htm" ) && !link.text().equals( "" ) )
				internalLinksSet.add( StringEscapeUtils.unescapeHtml4( link.text() ) );
		}
		for( Element link : doc.select( "div[class=card-summary-content] > div > a[href^=/view/]" ) )
		{
			if( link.hasAttr( "href" ) && link.attr( "href" ).endsWith( "htm" ) && !link.text().equals( "" ) )
				internalLinksSet.add( StringEscapeUtils.unescapeHtml4( link.text() ) );
		}
		
		return new ArrayList<String>( internalLinksSet );
	}
	
	private String getValidURL( String url )
	{
		try
		{
			URI uri = new URI( url );
			return uri.toString();
		}
		catch( URISyntaxException e )
		{
			return null;
		}
	}

	@Override
	public ArrayList<String> getExternalLinks()
	{
		ArrayList<String> outerLinks = new ArrayList<String>();

		for( Element link : doc.select( "div[class*=main-body]" ).select( "a" ) )
			if( link.hasAttr( "href" ) )
			{
				String tmp = link.attr( "href" ).toLowerCase();
				if( !tmp.startsWith( base + "view/" ) && tmp.startsWith( "http" ) )
				{
					String url = getValidURL( tmp );
					if( url != null )
						outerLinks.add( url );
				}
			}

		return outerLinks;
	}

	@Override
	public ArrayList<String> getRelatedPages()
	{
		ArrayList<String> relatedPages = new ArrayList<String>();
		for( Element relat : doc.select( "dl#relatedLemmaDown > dd > div[class=word_more_con]" ).select( "a" ) )
			if( relat.hasAttr( "href" ) && relat.attr( "href" ).startsWith( "/view/" ) && !relat.text().equals( "" ) )
				relatedPages.add( StringEscapeUtils.unescapeHtml4( relat.text() ) );

		return relatedPages;
	}

	@Override
	public boolean isDisambiguationPage()
	{
		return !doc.select( "dfn[class=sprite]" ).isEmpty();
	}
	
	@Override
	public ArrayList<String> getDisambiguations()
	{
		ArrayList<String> disambiguations = new ArrayList<String>();
		
		for( Element link : doc.select( "ol[data-nslog-type=503] > li > a" ) )
		{
			String tmp = getLabel() + "[" + link.text() + "]";
			disambiguations.add( tmp );
		}

		return disambiguations;
	}
	
	public ArrayList<String> getDisambiguationAnchors()
	{
		ArrayList<String> disambiguationAnchors = new ArrayList<String>();
		
		for( Element link : doc.select( "ol[data-nslog-type=503] > li > a" ) )
		{
			disambiguationAnchors.add( link.attr( "href" ) );
		}

		return disambiguationAnchors;
	}
	
	public ArrayList<Article> BaiduDisParse() 
	{
		ArrayList<Article> disArticles = new ArrayList<Article>();

		ArrayList<String> disList = getDisambiguations();
		ArrayList<String> anchorList = getDisambiguationAnchors();
		ArrayList<String> list = new ArrayList<String>();
		for( Element e : doc.select( "div[class*=polysemy-item-cnt]" ) )
			list.add( e.outerHtml() );
		
		for( int i = 0; i < disList.size(); ++i )
		{
			ZhishiArticle article = new ZhishiArticle( URICenter.source_name_baidu );
			doc = Jsoup.parse( list.get( i ) );

			article.label = disList.get( i );
			article.articleLink = base + "view/" + fileName + anchorList.get( i );
			article.isRedirect = isDisRedirectPage();
			if( article.isRedirect )
			{
				article.redirect = article.label.substring( article.label.indexOf( "[" ) + 1, article.label.indexOf( "]" ) );
				disArticles.add( article );
				continue;
			}
			article.categories = getCategories();
			article.abs = getAbstract();
			article.relatedPages = getRelatedPages();
			article.imageInfo = getImageInfo();
			article.properties = getProperties();
			article.internalLinks = getInternalLinks();
			article.externalLinks = getDisExternalLinks();
			disArticles.add( article );
		}
		
		return disArticles;
	}
	
	public boolean isDisRedirectPage() {
		
		if( !doc.select( "div[class^=view-tip-pannel]" ).select( "a[href$=redirect]" ).isEmpty() )
			return true;
		if( !doc.select( "div[class^=view-tip-pannel]" ).select( "a[class$=synstd]" ).isEmpty() )
			return true;

		return false;
	}
	
	public ArrayList<String> getDisExternalLinks()
	{
		ArrayList<String> outerLinks = new ArrayList<String>();
		for( Element link : doc.select( "a" ) )
		{
			if( link.hasAttr( "href" ) )
			{
				String tmp = link.attr( "href" );
				if( !tmp.startsWith( base + "view/" ) && tmp.startsWith( "http" ) )
				{
					String url = getValidURL( tmp );
					if( url != null )
						outerLinks.add( url );
				}
			}
		}
		return outerLinks;
	}
}
