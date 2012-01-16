package me.zhishi.matcher;

import java.util.HashSet;

import me.zhishi.tools.TextTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SimCalculator
{
	public class Instance
	{
		public String Name;
		public HashSet<String> Names;
		public HashSet<String> TagedNames;
		public HashSet<String> Features;
		public JSONObject Attrs;
		
		public boolean hasFeatures;
		
		public Instance()
		{
			Names = new HashSet<String>();
			TagedNames = new HashSet<String>();
			Features = new HashSet<String>();
			
			hasFeatures = false;
		}
	}
	
	private Instance source;
	private Instance target;
	
	public SimCalculator()
	{
		source = new Instance();
		target = new Instance();
	}
	
	public boolean parseInstance( JSONObject obj, boolean flag ) throws JSONException
	{
		Instance ins = flag ? source : target;
		
		ins.Attrs = obj.getJSONObject( "attr" );
		if( ins.Attrs.has( "pageRedirect" ) || ins.Attrs.has( "isDisambiguation" ) )
			return false;
		
		String name = ins.Attrs.getString( "label" );
		if( ins.Attrs.has( "isSynonym" ) )
		{
			SemanticLabel sl = new SemanticLabel( name, obj.getString( "source" ) );
			name = sl.head;
			if( sl.features.size() > 0 )
			{
				ins.Features.addAll( sl.features );
				ins.hasFeatures = true;
			}
		}
		
		ins.Name = name;
		ins.Names.add( ins.Name );
		
		if( ins.Attrs.has( "aliases" ) )
		{
			JSONArray aliases = ins.Attrs.getJSONArray( "aliases" );
			for( int i = 0; i < aliases.length(); ++i )
			{
				String alias = aliases.getString( i );
				if( !alias.equals( "" ) )
					ins.Names.add( alias );
			}
		}
		if( ins.Attrs.has( "categories" ) )
		{
			for( String n : ins.Names )
			{
				JSONArray categories = ins.Attrs.getJSONArray( "categories" );
				for( int i = 0; i < categories.length(); ++i )
				{
					String category = categories.getString( i );
					ins.TagedNames.add( n + category );
					ins.TagedNames.add( category + n );
				}
			}
		}
		return true;
	}
	
	public double getSim( JSONObject source, JSONObject target )
	{
		
		try
		{
			if( !parseInstance( source, true ) )
				return 0.0;
			if( !parseInstance( target, false ) )
				return 0.0;
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}

		return semanticSim();
	}
	
	private double semanticSim()
	{
		double sim = 0.0;
		double simTemp = 0.0;

		if( source.Name.equals( target.Name ) )
			sim = 1.0;
		
		//Alias
		for( String sourcen : source.Names )
		{
			for( String targetn: target.Names )
			{
				simTemp = 0.0;
				if( sourcen.equals( targetn ) )
					simTemp = 0.95;
				
				if( simTemp > sim )
					sim = simTemp;
			}
		}
		
		//Taged Name
		double GNscore = 0.9;
		boolean tagMatch = false;
		simTemp = 0.0;
		for( String sourcen : source.Names )
		{
			for( String targetn: target.TagedNames )
			{
				if( sourcen.equals( targetn ) )
					simTemp = GNscore;
				
				if( simTemp > sim )
					tagMatch = true;
			}
		}
		for( String sourcen : source.TagedNames )
		{
			for( String targetn: target.Names )
			{
				if( sourcen.equals( targetn ) )
					simTemp = GNscore;
				
				if( simTemp > sim )
					tagMatch = true;
			}
		}
		if( tagMatch )
		{
			double overlapSim = overlapSim( 0 );
			double weight = 0.5;
			if( overlapSim >= 0.0 )
			{
				simTemp = simTemp - weight + overlapSim * weight;
				sim = simTemp > sim ? simTemp : sim;
			}
		}

		//Disambiguation
		if( source.hasFeatures || target.hasFeatures )
		{
			double overlapSim = overlapSim( 10 );
			double weight = 0.5;
			if( overlapSim > 0.0 )
			{
				simTemp = simTemp - weight + overlapSim * weight;
				sim = simTemp > sim ? simTemp : sim;
			}
			else
				sim = 0.0;
		}
		
		sim = sim > 1.0 ? 1.0 : sim;
		sim = sim < 0.0 ? 0.0 : sim;
		
		if( sim > 0.0 )
			return sim;
			
		return 0.0;
	}
	
	private double overlapSim( int threshold )
	{
		int intensionSim = intensionSim();
		double featuresSim = featuresSim();
		
		double overlap = intensionSim + featuresSim * 40;
		if( overlap >= threshold )
			return 1 - Math.pow( 1.01, -overlap );
		
		return 0;
	}
	
	private int intensionSim()
	{
		try
		{
			HashSet<String> sourceRelatedLemmas = JSONArrayToSet( source.Attrs.getJSONArray( "relatedLemmas" ) );
			HashSet<String> targetRelatedLemmas = JSONArrayToSet( target.Attrs.getJSONArray( "relatedLemmas" ) );
			return sharedElementNum( sourceRelatedLemmas, targetRelatedLemmas );
			
//			int shareRLnum = sharedElementNum( sourceRelatedLemmas, targetRelatedLemmas );
//			
//			if( shareRLnum >= 10 )
//				return 1 - Math.pow( 1.01, -shareRLnum );
		}
		catch( JSONException e )
		{
			return 0;
		}
	}
	
	private double featuresSim()
	{
		try
		{
			if( sharedElementNum( source.Features, target.Features ) >= 1 )
				return 10000;
			
			HashSet<String> sourceFeatureTags;
			HashSet<String> targetFeatureTags;
			if( source.hasFeatures )
				sourceFeatureTags = JSONArrayToSet( source.Attrs.getJSONArray( "featureTags" ) );
			else
				sourceFeatureTags = new HashSet<String>();
			if( target.hasFeatures )
				targetFeatureTags = JSONArrayToSet( target.Attrs.getJSONArray( "featureTags" ) );
			else
				targetFeatureTags = new HashSet<String>();
			
			int shareTagsNum = sharedElementNum( sourceFeatureTags, targetFeatureTags );
			
//			HashSet<String> sourceInfoboxValues = JSONArrayToSet( source.Attrs.getJSONArray( "infoboxValues" ) );
//			HashSet<String> targetInfoboxValues = JSONArrayToSet( target.Attrs.getJSONArray( "infoboxValues" ) );
			
			int abs1 = source.Attrs.has( "abstract" ) ? tagInAbstract( targetFeatureTags, source.Attrs.getString( "abstract" ) ) : 0;
			int abs2 = target.Attrs.has( "abstract" ) ? tagInAbstract( sourceFeatureTags, target.Attrs.getString( "abstract" ) ) : 0;
			int absNum = abs1 + abs2;
			
			double num = shareTagsNum + absNum / 2.0;
			if( num >= 2 )
//				return 1 - Math.pow( 2, -num );
			
			return num;
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	private int tagInAbstract( HashSet<String> tags, String abs )
	{
		int num = 0;
		for( String tag : tags )
		{
			if( abs.contains( tag ) )
				num++;
		}
		return num;
	}
	
	private HashSet<String> JSONArrayToSet( JSONArray array )
	{
		HashSet<String> set = new HashSet<String>();
		try
		{
			for( int i = 0; i < array.length(); ++i )
			{
				if( !array.getString( i ).matches( TextTools.ignorablePunctuations ) )
					set.add( array.getString( i ) );
			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return set;
	}
	
	private int sharedElementNum( HashSet<String> source, HashSet<String> target )
	{
		HashSet<String> Temp = new HashSet<String>( source );
		Temp.retainAll( target );
		return Temp.size();
	}
	
	public static void main( String[] args )
	{
//		System.out.println( (1 - Math.pow( 1.01, -0 ))/2.0+0.5 );
	}
}
