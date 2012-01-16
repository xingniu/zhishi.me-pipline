package me.zhishi.matcher;

import java.util.HashSet;

import me.zhishi.tools.URICenter;

public class SemanticLabel
{
	public String head;
	public HashSet<String> features;
//	public HashSet<String> restrictions;
	
	private String disStart;
	private String disEnd;
	
	private void getSemanticLabel( String label )
	{
		if( label.contains( disStart ) )
		{
			head = label.substring( 0, label.lastIndexOf( disStart ) );
			if( label.contains( disEnd ) )
				features.add( label.substring( label.lastIndexOf( disStart )+disStart.length(), label.lastIndexOf( disEnd ) ) );
		}
		else
			head = label;
	}
	
	public SemanticLabel( String label, String source )
	{
		features = new HashSet<String>();
		
		if( source.equals( URICenter.source_name_baidu ) || source.equals( URICenter.source_name_hudong ) )
		{
			disStart = "[";
			disEnd = "]";
		}
		else if( source.equals( URICenter.source_name_zhwiki ) )
		{
			disStart = " (";
			disEnd = ")";
		}
		getSemanticLabel( label );
	}
	
	public static void main( String[] args )
	{
		String s = "李宁 (李兆基女婿)";
		System.out.println( (new SemanticLabel( s, URICenter.source_name_zhwiki )).head );
		System.out.println( (new SemanticLabel( s, URICenter.source_name_zhwiki )).features.toArray()[0] );
	}
}
