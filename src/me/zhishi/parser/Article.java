package me.zhishi.parser;

import java.util.ArrayList;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.StringPair;
import me.zhishi.tools.TripleWriter;

public class Article
{
	public String source;
	
	public String label;
	public String abs;
	public String redirect;
	public ArrayList<StringPair> pictures;
	public ArrayList<StringPair> properties;
//	public ArrayList<StringPair> alias;
	public ArrayList<String> categories;
	public ArrayList<String> internalLinks;
	public ArrayList<String> externalLinks;
	public ArrayList<String> relatedLabels;
	
	public boolean isDisambiguationPage;
	public ArrayList<Article> disambiguationArticles;
	
	public Article( String source )
	{
		this.source = source;
		
		label = null;
		abs = null;
		redirect = null;
		pictures = new ArrayList<StringPair>();
		properties = new ArrayList<StringPair>();
//		alias = new ArrayList<StringPair>();
		categories = new ArrayList<String>();
		internalLinks = new ArrayList<String>();
		externalLinks = new ArrayList<String>();
		relatedLabels = new ArrayList<String>();
		
		isDisambiguationPage = false;
	}
	
	public ArrayList<String> toTriples()
	{
		ArrayList<String> tripleList = new ArrayList<String>();
		URICenter ic = new URICenter( source );
		
		if( label != null && !label.equals( "" ) )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_label, label ) );
		}
		else
			return tripleList;
		
		for( String cat : categories )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_article_category, ic.getCategoryURI( cat ) ) );
		}
		
		//TODO: other contents

		return tripleList;
	}
}
