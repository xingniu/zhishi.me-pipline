package me.zhishi.parser;

import java.util.ArrayList;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.StringPair;
import me.zhishi.tools.TripleWriter;

public class ZhishiArticle implements Article
{
	public String source;
	
	public String label;
	public String abs;
	public String redirect;
	public ArrayList<StringPair> pictures;
	public ArrayList<StringPair> properties;
	public ArrayList<String> categories;
	public ArrayList<String> internalLinks;
	public ArrayList<String> externalLinks;
	public ArrayList<String> relatedPages;
	public ArrayList<String> disambiguationLabels;
	
	public boolean isDisambiguationPage;
	public boolean isRedirect;
	public ArrayList<Article> disambiguationArticles;
	
	public ZhishiArticle( String source )
	{
		this.source = source;
		
		label = null;
		abs = null;
		redirect = null;
		
		isDisambiguationPage = false;
		isRedirect = false;
	}
	
	@Override
	public ArrayList<String> toTriples()
	{
		ArrayList<String> tripleList = new ArrayList<String>();
		URICenter ic = new URICenter( source );
		
		if ( isRedirect )
		{
			if (redirect != null && !redirect.equals("")) {
				tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( redirect ), URICenter.predicate_redirect, ic.getResourceURI( label ) ) );
				tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( redirect ), URICenter.predicate_label, redirect ) );
			}
			if ( !isDisambiguationPage )
				return tripleList;
		}
		
		if ( isDisambiguationPage )
		{
			if( label != null && !label.equals( "" ) )
			{
				tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_label, label ) );
				for (String disam : disambiguationLabels) {
					tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_pageDisambiguates, ic.getResourceURI( disam ) ) );
				}
			}
			else
				return tripleList;
			if (source.equals(URICenter.source_name_baidu)){
				for (int i = 0; i < disambiguationArticles.size(); ++i){
					ZhishiArticle article = new ZhishiArticle( URICenter.source_name_baidu );
					article.toTriples();
				}
			}
			
			return tripleList;
			
		}
		
		if( label != null && !label.equals( "" ) )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_label, label ) );
		}
		else
			return tripleList;
		
		if( abs != null && !abs.equals( "" ) )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_abstract, abs ) );
		}
		
		for( String cat : categories )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_article_category, ic.getCategoryURI( cat ) ) );
		}
		
		for( String relat : relatedPages )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_relatedPages, ic.getResourceURI( relat ) ) );
		}
		
		for( int i = 0; i < pictures.size(); ++i)
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_pictures,  pictures.get(i).first ) );
			tripleList.add( TripleWriter.getStringValueTriple(  pictures.get(i).first, URICenter.predicate_pictureLabels, pictures.get(i).second ) );
		}
		
		for ( int i = 0; i < properties.size(); ++i )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), ic.getPropertyPredicate( properties.get(i).first ), properties.get(i).second ) );
		}
		
		for ( String innerlink : internalLinks )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_internalLinks, ic.getResourceURI( innerlink ) ) );
		}
		
		for ( String outerLink : externalLinks)
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_externalLinks, outerLink ));
		}

		//TODO: other contents

		return tripleList;
	}
}
