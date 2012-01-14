package me.zhishi.tools;

public class URICenter
{
	public static String domainName = "http://zhishi.me/";
	
	public static String source_name_baidu = "Baidu";
	public static String source_name_hudong = "Hudong";
	public static String source_name_zhwiki = "zhWiki";
	
	public static String namespace_baidu = "http://zhishi.me/baidubaike/";
	public static String namespace_hudong = "http://zhishi.me/hudongbaike/";
	public static String namespace_zhwiki = "http://zhishi.me/zhwiki/";
	
	public static String predicate_label = "<http://www.w3.org/2000/01/rdf-schema#label>";
	public static String predicate_articleLink = "<http://xmlns.com/foaf/0.1/page>";
	public static String predicate_abstract = "<http://zhishi.me/ontology/abstract>";
	public static String predicate_relatedPage = "<http://zhishi.me/ontology/relatedPage>";
	public static String predicate_redirect = "<http://zhishi.me/ontology/pageRedirects>";
	public static String predicate_thumbnail = "<http://zhishi.me/ontology/thumbnail>";
	public static String predicate_pictureLabels = "<http://www.w3.org/2000/01/rdf-schema#label>";
	public static String predicate_internalLink = "<http://zhishi.me/ontology/internalLink>";
	public static String predicate_externalLink = "<http://zhishi.me/ontology/externalLink>";
	public static String predicate_disambiguation = "<http://zhishi.me/ontology/pageDisambiguates>";
	// TODO : Pay attention here
	public static String predicate_article_category_old = "<http://purl.org/dc/terms/subject>";
	public static String predicate_article_category = "<http://zhishi.me/ontology/category>";
	
	public static String predicate_foaf_primaryTopic = "<http://xmlns.com/foaf/0.1/primaryTopic>";
	public static String predicate_dc_language = "<http://purl.org/dc/elements/1.1/language>";
	public static String predicate_skos_broader = "<http://www.w3.org/2004/02/skos/core#broader>";
	public static String predicate_skos_narrower = "<http://www.w3.org/2004/02/skos/core#narrower>";
	public static String predicate_skos_prefLabel = "<http://www.w3.org/2004/02/skos/core#prefLabel>";
	public static String predicate_rdf_type = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
	
	public static String class_skos_concept = "http://www.w3.org/2004/02/skos/core#Concept";
	
	private String namespace;
	
	public URICenter( String source )
	{
		if( source.equals( source_name_baidu ) )
			namespace = namespace_baidu;
		else if( source.equals( source_name_hudong ) )
			namespace = namespace_hudong;
		else if( source.equals( source_name_zhwiki ) )
			namespace = namespace_zhwiki;
	}
	
	public String getResourceURI( String label )
	{
		return namespace + "resource/" + zhishiEncode( label );
	}
	
	public String getCategoryURI( String label )
	{
		return namespace + "category/" + zhishiEncode( label );
	}
	
	public String getPropertyPredicate( String label )
	{
		return "<" + namespace + "property/" + zhishiEncode( label ) + ">";
	}
	
	public static String zhishiEncode( String str )
	{
		return TextTools.encoder( str ).replaceAll( "\\+", "_" );
	}
	
	public static String zhishiDecode( String str )
	{
		return TextTools.decoder( str.replaceAll( "_", "+" ) );
	}
}
