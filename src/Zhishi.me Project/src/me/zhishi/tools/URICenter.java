package me.zhishi.tools;

public class URICenter
{
	public static String domainName = "http://zhishi.me/";
	
	public static String source_name_zhishi = "Zhishi";
	public static String source_name_baidu = "Baidu";
	public static String source_name_hudong = "Hudong";
	public static String source_name_zhwiki = "zhWiki";
	
	public static String namespace_zhishi = domainName;
	public static String namespace_baidu = namespace_zhishi + "baidubaike/";
	public static String namespace_hudong = namespace_zhishi + "hudongbaike/";
	public static String namespace_zhwiki = namespace_zhishi + "zhwiki/";
	public static String namespace_ontology = namespace_zhishi + "ontology/";
	
	public static String namespace_dbpedia = "http://dbpedia.org/";
	public static String namespace_dbpedia_ontology = namespace_dbpedia + "ontology/";
	public static String namespace_zhdbpedia = "http://zh.dbpedia.org/";
	public static String namespace_zhdbpedia_resource = namespace_zhdbpedia + "resource/";
	public static String namespace_zhdbpedia_property = namespace_zhdbpedia + "property/";
	
	public static String predicate_category = namespace_ontology + "category";
	public static String predicate_abstract = namespace_ontology + "abstract";
	public static String predicate_relatedPage = namespace_ontology + "relatedPage";
	public static String predicate_redirect = namespace_ontology + "pageRedirects";
	public static String predicate_internalLink = namespace_ontology + "internalLink";
	public static String predicate_externalLink = namespace_ontology + "externalLink";
	public static String predicate_disambiguation = namespace_ontology + "pageDisambiguates";
	public static String predicate_depictionThumbnail = namespace_ontology + "depictionThumbnail";
	public static String predicate_relatedImage = namespace_ontology + "relatedImage";
	
	public static String predicate_labelWithUnit = namespace_ontology + "labelWithUnit";
	public static String predicate_prefUnit = namespace_ontology + "prefUnit";
	
	public static String predicate_temp_exception = "exception";
	public static String predicate_temp_featureTags = "featureTags";
	public static String predicate_temp_lookup = "lookup";
	public static String predicate_temp_unit = "unit";
	
	public static String predicate_dbpedia_abstract = namespace_dbpedia_ontology + "abstract";
	public static String predicate_dbpedia_wikiPageExternalLink = namespace_dbpedia_ontology + "wikiPageExternalLink";
	public static String predicate_dbpedia_wikiPageWikiLink = namespace_dbpedia_ontology + "wikiPageWikiLink";
	public static String predicate_dbpedia_wikiPageRedirects = namespace_dbpedia_ontology + "wikiPageRedirects";
	
	public static String predicate_foaf_primaryTopic = "http://xmlns.com/foaf/0.1/primaryTopic";
	public static String predicate_foaf_isPrimaryTopicOf = "http://xmlns.com/foaf/0.1/isPrimaryTopicOf";
	public static String predicate_foaf_depiction = "http://xmlns.com/foaf/0.1/depiction";
//	public static String predicate_foaf_page = "http://xmlns.com/foaf/0.1/page";
	public static String predicate_foaf_thumbnail = "http://xmlns.com/foaf/0.1/thumbnail";
	public static String predicate_dc_language = "http://purl.org/dc/elements/1.1/language";
	public static String predicate_dc_rights = "http://purl.org/dc/elements/1.1/rights";
	public static String predicate_dc_subject = "http://purl.org/dc/terms/subject";
	public static String predicate_skos_broader = "http://www.w3.org/2004/02/skos/core#broader";
	public static String predicate_skos_narrower = "http://www.w3.org/2004/02/skos/core#narrower";
	public static String predicate_skos_prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";
	public static String predicate_rdf_type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static String predicate_rdfs_label = "http://www.w3.org/2000/01/rdf-schema#label";
	public static String predicate_rdfs_isDefinedBy = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
	public static String predicate_rdfs_domain = "http://www.w3.org/2000/01/rdf-schema#domain";
	public static String predicate_rdfs_range = "http://www.w3.org/2000/01/rdf-schema#range";
	public static String predicate_owl_sameAs = "http://www.w3.org/2002/07/owl#sameAs";
	
	public static String property_geo_lat = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	public static String property_geo_long = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	
	public static String datatype_xmls_string = "http://www.w3.org/2001/XMLSchema#string";
	public static String datatype_xmls_int = "http://www.w3.org/2001/XMLSchema#int";
	public static String datatype_xmls_double = "http://www.w3.org/2001/XMLSchema#double";
	public static String datatype_xmls_date = "http://www.w3.org/2001/XMLSchema#date";
	
	public static String object_owl_Thing = "http://www.w3.org/2002/07/owl#Thing";
	public static String object_owl_DatatypeProperty = "http://www.w3.org/2002/07/owl#DatatypeProperty";
	public static String object_owl_ObjectProperty = "http://www.w3.org/2002/07/owl#ObjectProperty";
	public static String object_rdf_Property = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
	public static String object_foaf_Image = "http://xmlns.com/foaf/0.1/Image";
	public static String object_foaf_Document = "http://xmlns.com/foaf/0.1/Document";
	public static String object_zhishi = namespace_ontology;
	
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
		return getURIByKey( "resource", label );
	}
	
	public String getCategoryURI( String label )
	{
		return getURIByKey( "category", label );
	}
	
	public String getPropertyPredicate( String label )
	{
		return getURIByKey( "property", label );
	}
	
	public String getURIByKey( String key, String label )
	{
		return namespace + key + "/" + zhishiEncode( label );
	}
	
	public static String zhishiEncode( String str )
	{
		return TextTools.encoder( str ).replaceAll( "\\+", "_" );
	}
	
	public static String zhishiDecode( String str )
	{
		return TextTools.decoder( str.replaceAll( "_", "+" ) );
	}
	
	public static String getURIContent( String str )
	{
		return zhishiDecode( str.substring( str.lastIndexOf( "/" )+1, str.indexOf( ">" ) ) );
	}
}
