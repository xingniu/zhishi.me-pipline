package me.zhishi.tools;

public class FileNames
{
	public static String baiduFileName = "baidubaike";
	public static String hudongFileName = "hudongbaike";
	public static String zhwikiFileName = "zhwiki";
	
	private String source;
	private String sourcefileName;
	private String releaseVersion;
	
	private GlobalConfigs globleConfigs;
	
	public void setSourcefileName( String source )
	{
		if( source.equals( "Baidu" ) )
			sourcefileName = baiduFileName;
		else if( source.equals( "Hudong" ) )
			sourcefileName = hudongFileName;
		else if( source.equals( "zhWiki" ) )
			sourcefileName = zhwikiFileName;
	}
	
	public FileNames( double releaseV )
	{
		releaseVersion = Double.toString( releaseV );
		String dumpVersion = null;
		if( releaseV >= 2.0 && releaseV < 3.0 )
			dumpVersion = "2011";
		globleConfigs = new GlobalConfigs( dumpVersion );
	}
	
	public FileNames( double releaseV, String src )
	{
		this( releaseV );
		source = src;
		setSourcefileName( src );
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
	
	public void setReleaseVersion( String newVersion )
	{
		releaseVersion = newVersion;
	}
	
	public String getAbstractFileName()
	{
		return globleConfigs.getNTriplesPath( source )+releaseVersion+"_"+sourcefileName+"_abstracts_zh.nt";
	}
}
