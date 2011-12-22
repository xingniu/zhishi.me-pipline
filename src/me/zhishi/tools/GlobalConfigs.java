package me.zhishi.tools;

public class GlobalConfigs
{
	// Default version info
	private String dumpVersion;
	private String dumpPath;
	
	public static String projectDataPath = "//POSEIDON/Share/Groups/Semantic Group/Chinese LOD/";
	public static String domainName = "http://zhishi.me/";
	
	public static String Hudong = "Hudong";
	public static String Baidu = "Baidu";
	public static String zhWiki = "zhWiki";
	
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
