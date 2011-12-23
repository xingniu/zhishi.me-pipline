package me.zhishi.tools;

public class GlobalConfigs
{
	// Default version info
	private String dumpVersion;
	private String dumpPath;
	
	public static String projectDataPath = "//POSEIDON/Share/Groups/Semantic Group/Chinese LOD/";
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
	
	public GlobalConfigs( String dumpV )
	{
		dumpVersion = dumpV;
		dumpPath = projectDataPath + dumpVersion + "/";
	}
	
	public void setDumpVersion( String newVersion )
	{
		dumpVersion = newVersion;
		dumpPath = projectDataPath + dumpVersion + "/";
	}

	public String getNTriplesPath( String source )
	{
		return dumpPath + source + "/NTriples/";
	}
}
