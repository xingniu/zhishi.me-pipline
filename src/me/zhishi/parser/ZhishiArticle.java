package me.zhishi.parser;

import java.util.ArrayList;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.StringPair;
import me.zhishi.tools.file.TripleWriter;

public class ZhishiArticle implements Article
{
	public static class ImageInfo
	{
		public String depiction;
		public String depictionThumbnail;
		public ArrayList<String> relatedImages;
		public ArrayList<StringPair> thumbnails;
		public ArrayList<StringPair> rights;
		public ArrayList<StringPair> labels;
		
		public ImageInfo()
		{
			depiction = null;
			depictionThumbnail = null;
			relatedImages = new ArrayList<String>();
			thumbnails = new ArrayList<StringPair>();
			rights = new ArrayList<StringPair>();
			labels = new ArrayList<StringPair>();
		}
	}
	
	public String source;
	
	public String label;
	public String articleLink;
	public String abs;
	public String redirect;
	public ImageInfo imageInfo;
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
		articleLink = null;
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
			if( redirect != null )
			{
				tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_rdfs_label, label ) );
				tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_foaf_isPrimaryTopicOf, articleLink ) );
				tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_redirect, ic.getResourceURI( redirect ) ) );
			}
			if( !isDisambiguationPage )
				return tripleList;
		}
		
		if ( isDisambiguationPage )
		{
			if( label != null )
			{
				tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_rdfs_label, label ) );
				tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_foaf_isPrimaryTopicOf, articleLink ) );
				for( String disam : disambiguationLabels )
				{
					tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_disambiguation, ic.getResourceURI( disam ) ) );
				}
			}
			else
				return tripleList;
			
			if( source.equals( URICenter.source_name_baidu ) )
			{
				for( Article article : disambiguationArticles )
				{
					tripleList.addAll( article.toTriples() );
				}
			}
			
			return tripleList;
		}
		
		if( label != null )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_rdfs_label, label ) );
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_foaf_isPrimaryTopicOf, articleLink ) );
		}
		else
			return tripleList;
		
		if( abs != null )
		{
			tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), URICenter.predicate_abstract, abs ) );
		}
		
		for( String cat : categories )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_category, ic.getCategoryURI( cat ) ) );
		}
		
		for( String relat : relatedPages )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_relatedPage, ic.getResourceURI( relat ) ) );
		}
		
		for ( int i = 0; i < properties.size(); ++i )
		{
			if( !properties.get(i).first.equals( "" ) )
				tripleList.add( TripleWriter.getStringValueTriple( ic.getResourceURI( label ), ic.getPropertyPredicate( properties.get(i).first ), properties.get(i).second ) );
		}
		
		for ( String innerlink : internalLinks )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_internalLink, ic.getResourceURI( innerlink ) ) );
		}
		
		for ( String outerLink : externalLinks)
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_externalLink, outerLink ));
		}
		
		if( imageInfo.depiction != null )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_foaf_depiction, imageInfo.depiction ) );
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_depictionThumbnail, imageInfo.depictionThumbnail ) );
		}
		for( String relatedImage : imageInfo.relatedImages )
		{
			tripleList.add( TripleWriter.getResourceObjectTriple( ic.getResourceURI( label ), URICenter.predicate_relatedImage, relatedImage ) );
		}
		for( int i = 0; i < imageInfo.labels.size(); ++i )
		{
			if( !imageInfo.labels.get(i).second.equals( "" ) )
				tripleList.add( TripleWriter.getStringValueTriple( imageInfo.labels.get(i).first, URICenter.predicate_rdfs_label, imageInfo.labels.get(i).second ) );
			if( imageInfo.rights.get(i).second != null )
				tripleList.add( TripleWriter.getResourceObjectTriple( imageInfo.rights.get(i).first, URICenter.predicate_dc_rights, imageInfo.rights.get(i).second ) );
			tripleList.add( TripleWriter.getResourceObjectTriple( imageInfo.thumbnails.get(i).first, URICenter.predicate_foaf_thumbnail, imageInfo.thumbnails.get(i).second ) );
		}

		//TODO: other contents

		return tripleList;
	}
}
