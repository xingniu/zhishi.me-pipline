package me.zhishi.tools;

public class IRICenter
{
	public static String domainName = "http://zhishi.me/";
	
	public static String source_name_baidu = "Baidu";
	public static String source_name_hudong = "Hudong";
	public static String source_name_zhwiki = "zhWiki";
	
	public static String namespace_baidu = "http://zhishi.me/baidubaike/";
	public static String namespace_hudong = "http://zhishi.me/hudongbaike/";
	public static String namespace_zhwiki = "http://zhishi.me/zhwiki/";
	
	public static String predicate_label = "<http://www.w3.org/2000/01/rdf-schema#label>";
	// TODO : Pay attention here
	public static String predicate_article_category_old = "<http://purl.org/dc/terms/subject>";
	public static String predicate_article_category = "<http://zhishi.me/ontology/category>";
	
	private String namespace;
	
	public IRICenter( String source )
	{
		if( source.equals( source_name_baidu ) )
			namespace = namespace_baidu;
		else if( source.equals( source_name_hudong ) )
			namespace = namespace_hudong;
		else if( source.equals( source_name_zhwiki ) )
			namespace = namespace_zhwiki;
	}
	
	public String getResourceIRI( String label )
	{
		return namespace + "resource/" + TextTools.getUnicode( label );
	}
	
	public String getCategoryIRI( String label )
	{
		return namespace + "category/" + TextTools.getUnicode( label );
	}
}
