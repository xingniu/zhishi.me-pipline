package me.zhishi.tools;

import java.util.HashMap;

public class Path
{
	// Poseidon
	public static String projectDataPath = "//POSEIDON/Share/Groups/Semantic Group/Chinese LOD/";
	
	// Hadoop HDFS
	public static String hdfs_username = "APEXLAB-xingniu";
	public static String hdfs_fsName = "hdfs://172.16.7.14";
	public static String hdfs_projectDataPath = "/Users/xingniu/CLOD/";
	private boolean isHDFS = false;
	
	public static String baiduFileName = "baidubaike";
	public static String hudongFileName = "hudongbaike";
	public static String zhwikiFileName = "zhwiki";
	public static String zhishiFileName = "zhishi";
	
	private String source;
	private String sourcefileName;
	private double releaseVersion;
	
	private String dumpVersion;
	private String dumpPath;
	
	public static int hudongMax = 364;
	public static int baiduMax = 714;
	
	private static HashMap<String,String> fileNameMap = new HashMap<String,String>();
	static
	{
		fileNameMap.put( "label", "labels" );
		fileNameMap.put( "category", "article_categories" );
		fileNameMap.put( "abstract", "abstracts" );
		fileNameMap.put( "relatedPage", "related_pages" );
		fileNameMap.put( "internalLink", "internal_links" );
		fileNameMap.put( "externalLink", "external_links" );
		fileNameMap.put( "redirect", "redirects" );
		fileNameMap.put( "disambiguation", "disambiguations" );
		fileNameMap.put( "articleLink", "article_links" );
		fileNameMap.put( "categoryLabel", "category_labels" );
		fileNameMap.put( "skosCat", "skos_categories" );
		fileNameMap.put( "image", "images" );
		fileNameMap.put( "imageInfo", "image_information" );
		fileNameMap.put( "infobox", "infobox_properties" );
		fileNameMap.put( "infoboxText", "infobox_properties_text" );
		fileNameMap.put( "propertyLabel", "infobox_property_definitions" );
		
		fileNameMap.put( "baiduLink", "baidubaike_links" );
		fileNameMap.put( "hudongLink", "hudongbaike_links" );
		fileNameMap.put( "zhwikiLink", "zhwiki_links" );
		
		fileNameMap.put( "ontology", "ontology" );
		fileNameMap.put( "lookup", "lookup" );
		
		fileNameMap.put( "featureTags", "FEATURE_TAGS" );
		fileNameMap.put( "exception", "EXCEPTION" );
	}
	
	public void setSourcefileName( String source )
	{
		if( source.equals( URICenter.source_name_baidu ) )
			sourcefileName = baiduFileName;
		else if( source.equals( URICenter.source_name_hudong ) )
			sourcefileName = hudongFileName;
		else if( source.equals( URICenter.source_name_zhwiki ) )
			sourcefileName = zhwikiFileName;
		else if( source.equals( URICenter.source_name_zhishi ) )
			sourcefileName = zhishiFileName;
	}
	
	public void init( double releaseVersion )
	{
		this.releaseVersion = releaseVersion;
		
		String dumpVersion = null;
		if( releaseVersion >= 2.0 && releaseVersion < 2.9 )
			dumpVersion = "2011";
		else if( releaseVersion >= 2.9 && releaseVersion <= 3.0 )
			dumpVersion = "2011.12";
		else if( releaseVersion >= 3.1 )
			dumpVersion = "2012";
		this.dumpVersion = dumpVersion;
		
		dumpPath = projectDataPath + dumpVersion + "/";	
	}
	
	public Path( double releaseVersion )
	{
		init( releaseVersion );
		source = URICenter.source_name_zhishi;
		setSourcefileName( source );
		isHDFS = false;
	}
	
	public Path( double releaseVersion, boolean isHDFS )
	{
		this( releaseVersion );
		source = URICenter.source_name_zhishi;
		setSourcefileName( source );
		this.isHDFS = isHDFS;
	}
	
	public Path( double releaseVersion, String source )
	{
		this( releaseVersion );
		this.source = source;
		setSourcefileName( source );
		isHDFS = false;
	}
	
	public Path( double releaseVersion, String source, boolean isHDFS )
	{
		this( releaseVersion, source );
		this.isHDFS = isHDFS;
	}
	
//	public Path( String dumpVersion )
//	{
//		this.dumpVersion = dumpVersion;
//		dumpPath = projectDataPath + dumpVersion + "/";
//	}
//	
//	public Path( String dumpVersion, String source  )
//	{
//		this( dumpVersion );
//		this.source = source;
//		setSourcefileName( source );
//	}
	
	public void setDumpVersion( String newVersion )
	{
		dumpVersion = newVersion;
		dumpPath = projectDataPath + dumpVersion + "/";
	}
	
	public String getDumpVersion()
	{
		return dumpVersion;
	}
	
	public String getDumpPath()
	{
		return dumpPath + source + "/";
	}
	
	public void setReleaseVersion( double newVersion )
	{
		releaseVersion = newVersion;
		init( releaseVersion );
	}
	
	public double getReleaseVersion()
	{
		return releaseVersion;
	}
	
	public void setSource( String src )
	{
		source = src;
		setSourcefileName( src );
	}
	
	public String getSource()
	{
		return source;
	}
	
	public String getMainPageFolder()
	{
		if( isHDFS )
			return hdfs_projectDataPath + "BaikePages/" + source + "/";
		else
			return dumpPath + source + "/MainPages/";
	}
	
	public String getMainPageFile( String file )
	{
		return getMainPageFolder() + file + ".tar.bz2";
	}
	
	public String getRawStructuredDataFolder()
	{
		return hdfs_projectDataPath + "RawStructuredData/" + source + "/";
	}
	
	public String getMatchingFolder()
	{
		return hdfs_projectDataPath + "Matching/";
	}
	
	public String getMatchingFile()
	{
		return getMatchingFolder() + "all_matches.txt";
	}
	
	public String getJSONFolder()
	{
		return getMatchingFolder() + "JSON/";
	}
	
	public String getNTriplesFolder()
	{
		if( isHDFS )
			return hdfs_projectDataPath + "NTriples/";
		else
			return dumpPath + source + "/NTriples/";
	}
	
	public String getNTriplesFile( String keyword )
	{
		return getNTriplesFolder() + getNTriplesFileName( keyword );
	}
	
	public String getNTriplesFileName( String keyword )
	{
		return releaseVersion+"_"+sourcefileName+"_" + fileNameMap.get( keyword ) + "_zh.nt";
	}
}
