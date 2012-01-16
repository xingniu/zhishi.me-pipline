package me.zhishi.matcher;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class KeysGenerator
{
	public static HashSet<String> getKeys( JSONObject info )
	{
		HashSet<String> set = new HashSet<String>();
		try
		{
			JSONObject attr = info.getJSONObject( "attr" );
			String name = attr.getString( "label" );
			if( attr.has( "isSynonym" ) )
				name = (new SemanticLabel( name, info.getString( "source" ) )).head;
			set.add( name );
			if( attr.has( "aliases" ) )
			{
				JSONArray aliases = attr.getJSONArray( "aliases" );
				for( int i = 0; i < aliases.length(); ++i )
				{
					String alias = aliases.getString( i );
					if( !alias.equals( "" ) )
						set.add( alias );
				}
			}
			HashSet<String> tempSet = new HashSet<String>( set );
			if( attr.has( "categories" ) )
			{
				for( String n : tempSet )
				{
					JSONArray categories = attr.getJSONArray( "categories" );
					for( int i = 0; i < categories.length(); ++i )
					{
						String category = categories.getString( i );
						set.add( n + category );
						set.add( category + n );
					}
				}
			}
//			if( attr.has( "relatedLemmas" ) )
//			{
//				JSONArray relatedLemmas = attr.getJSONArray( "relatedLemmas" );
//				for( int i = 0; i < relatedLemmas.length(); ++i )
//				{
//					String relatedLemma = relatedLemmas.getString( i );
//					set.add( relatedLemma );
//				}
//			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return set;
	}
}
