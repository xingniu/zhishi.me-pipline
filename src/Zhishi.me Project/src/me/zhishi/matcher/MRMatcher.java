package me.zhishi.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import me.zhishi.tools.URICenter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.json.JSONException;
import org.json.JSONObject;

public class MRMatcher
{
	public static double releaseVersion = 3.0;
	public static String initialMatchesFolder;
	public static String removeDuplicatePairsFolder;
	public static String removeDuplicateLeftFolder;
	public static String removeDuplicateRightFolder;
	public static String allMatchesFolder;
	public static String transferMatchesFolder;
	public static String matchingFile;
	static
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, true );
		String MatchingFolder = p.getMatchingFolder();
		initialMatchesFolder = MatchingFolder + "InitialMatch/";
		removeDuplicatePairsFolder = MatchingFolder + "RemovedDuplicatePairs/";
		removeDuplicateLeftFolder = MatchingFolder + "RemovedDuplicateLeft/";
		removeDuplicateRightFolder = MatchingFolder + "RemovedDuplicateRight/";
		allMatchesFolder = MatchingFolder + "AllMatches/";
		transferMatchesFolder = MatchingFolder + "TransferedMatches/";
		matchingFile = p.getMatchingFile();
	}
	
	public static class MatchingMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException,
				InterruptedException
		{
			JSONObject j;
			HashSet<String> set = null;
			try
			{
				j = new JSONObject( value.toString() );
				set = KeysGenerator.getKeys( j );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
			
			for( String seg : set )
			{
				context.write( new Text( seg.toLowerCase() ), value );
			}
		}
	}

	public static class MatchingReducer extends Reducer<Text, Text, Text, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException,
				InterruptedException
		{
			String sourceA = context.getConfiguration().get( "sourceA" );
			String sourceB = context.getConfiguration().get( "sourceB" );
			
			LinkedList<JSONObject> listA = new LinkedList<JSONObject>();
			LinkedList<JSONObject> listB = new LinkedList<JSONObject>();
			JSONObject j;
			
			try
			{
				for( Text val : values )
				{
					j = new JSONObject( val.toString() );
					if( j.getString( "source" ).equals( sourceA ) )
						listA.add( j );
					else if( j.getString( "source" ).equals( sourceB ) )
						listB.add( j );
				}
				
//				System.out.println( key.toString() );
//				System.out.println( listA.size() );
//				System.out.println( listB.size() );
				if( listB.size() > 1000 || listA.size() > 1000 )
					return;
				
				for( JSONObject jA : listA )
				{
					HashMap<String, Double> matchingMap = new HashMap<String, Double>();
					HashMap<String, String> infoMap = new HashMap<String, String>();
					
					for( JSONObject jB : listB )
					{
						String LA = jA.getJSONObject( "attr" ).getString( "label" );
						String LB = jB.getJSONObject( "attr" ).getString( "label" );
						
						SimCalculator sim = new SimCalculator();
						double confidence = sim.getSim( jA, jB );
						matchingMap.put( LA + "\t" + LB, confidence );
//						infoMap.put( LA + "\t" + LB, jA + "\t" + jB );
						infoMap.put( LA + "\t" + LB, jA.getString( "uri" ) + "\t" + jB.getString( "uri" ) );
					}
					
					@SuppressWarnings("unchecked")
					Map.Entry<String,Double> entry = (Entry<String, Double>) sort( matchingMap );
					if( entry != null )
					{
						if( entry.getValue() > 0.0 )
						{
							context.write( new Text( Double.toString( entry.getValue() ) ), new Text( infoMap.get( entry.getKey() ) ) );
//							context.write( new Text( Double.toString( entry.getValue() ) ), new Text( entry.getKey() ) );
						}
					}
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public static Map.Entry<?,Double> sort( HashMap<?,Double> map )
	{
		if( map.size() >= 1 )
		{
			ArrayList< Map.Entry<?,Double> > sortedMatches = new ArrayList< Map.Entry<?,Double> >( map.entrySet() );
			Collections.sort( sortedMatches, new Comparator< Map.Entry<?,Double> >()
			{
				public int compare(Map.Entry<?,Double> o1, Map.Entry<?,Double> o2)
				{
					return (o2.getValue() - o1.getValue()) > 0 ? 1 : 0; 
				}
			});
			
			double maxConf = sortedMatches.get( 0 ).getValue();
			if( sortedMatches.size() >= 2 )
			{
				if( maxConf > sortedMatches.get( 1 ).getValue() )
					return sortedMatches.get( 0 );
			}
			return sortedMatches.get( 0 );
		}
		return null;
	}
	
	public static class RemoveDuplicatePairsMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String info = value.toString();
			int split = info.indexOf( "\t" );

			if( split < 0 )
			{
				System.err.println( info );
				return;
			}
			Text Value = new Text( info.substring( 0, split ) );
			Text Key = new Text( info.substring( split + 1 ) );
			context.write( Key, Value );
		}
	}

	public static class RemoveDuplicatePairsReducer extends Reducer<Text, Text, Text, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			double confidence = 0.0;
			for( Text val : values )
			{
				double tempConf = Double.valueOf( val.toString() );
				if( tempConf > confidence )
					confidence = tempConf;
			}
			context.write( new Text( Double.toString( confidence ) ), key );
		}
	}
	
	public static class RemoveDuplicatePartsMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String info = value.toString();
			String segs[] = info.split( "\t" );
			
			if( segs.length != 3 )
				return;

			int index = context.getConfiguration().get( "removePart" ).equals( "left" ) ? 1 : 2;
			
			Text Key = new Text( segs[index] );
			Text Value = new Text( info );
			
			context.write( Key, Value );
		}
	}
	
	public static class RemoveDuplicatePartsReducer extends Reducer<Text, Text, Text, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashMap<String, Double> matchingMap = new HashMap<String, Double>();
			for( Text val : values )
			{
				String info = val.toString();
				int split = info.indexOf( "\t" );
				
				matchingMap.put( info.substring( split + 1 ), Double.valueOf( info.substring( 0, split ) ) );
			}
			
			@SuppressWarnings("unchecked")
			Map.Entry<String,Double> entry = (Entry<String, Double>) sort( matchingMap );
			if( entry != null )
			{
				if( entry.getValue() > 0.0 )
				{
					context.write( new Text( Double.toString( entry.getValue() ) ), new Text( entry.getKey() ) );
				}
			}
		}
	}
	
	public static class TransferSameAsMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String info = value.toString();
			String segs[] = info.split( "\t" );
			Text Left = new Text( segs[0]+'\t'+segs[2] );
			Text Right = new Text( segs[0]+'\t'+segs[1] );
			
			context.write( new Text( segs[1] ), Left );
			context.write( new Text( segs[2] ), Right );
		}
	}

	public static class TransferSameAsReducer extends Reducer<Text, Text, Text, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			String[] ns = { URICenter.namespace_baidu, URICenter.namespace_hudong, URICenter.namespace_zhwiki };
			String media = key.toString();
			int sourceN = 0;
			for( int i = 0; i <= 2; ++i )
			{
				if( media.contains( ns[i] ) )
					sourceN = i;
			}
			
			String neighbor[] = { "", "" };
			String confidence[] = { "", "" };
			for( Text val : values )
			{
				String targetSegs[] = val.toString().split( "\t" );
				int targetN = 0;
				for( int i = 0; i <= 2; ++i )
				{
					if( targetSegs[1].contains( ns[i] ) )
						targetN = i;
				}
				
				int index = ( targetN - sourceN + 2 ) % 3;
				confidence[index] = targetSegs[0];
				neighbor[index] = targetSegs[1];
			}
			
			if( !neighbor[0].equals( "" ) && !neighbor[1].equals( "" ) )
			{
				double newConf = Double.valueOf( confidence[0] ) * Double.valueOf( confidence[1] );
				context.write( new Text( Double.toString( newConf ) ), new Text( neighbor[0]+'\t'+neighbor[1] ) );
			}
		}
	}
	
	public static void match( String sourceA, String sourceB ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, true );
		String inputPath = p.getJSONFolder();
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( initialMatchesFolder ), true );
		
		conf.set( "sourceA", sourceA );
		conf.set( "sourceB", sourceB );
		Job job = new Job( conf, "IM: " + sourceA + " to " + sourceB );
		
		job.setNumReduceTasks( 30 );

		job.setJarByClass( MRMatcher.class );
		job.setMapperClass( MatchingMapper.class );
		job.setReducerClass( MatchingReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( initialMatchesFolder ) );

		job.waitForCompletion( true );
	}
	
	public static void RemoveDuplicatePairs( String info ) throws Exception
	{
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( removeDuplicatePairsFolder ), true );
		
		Job job = new Job( conf, "Removing duplicate pairs: " + info );

		job.setJarByClass( MRMatcher.class );
		job.setMapperClass( RemoveDuplicatePairsMapper.class );
		job.setReducerClass( RemoveDuplicatePairsReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( initialMatchesFolder ) );
		FileOutputFormat.setOutputPath( job, new Path( removeDuplicatePairsFolder ) );

		job.waitForCompletion( true );
	}
	
	public static void RemoveDuplicateParts( String part, String info ) throws Exception
	{
		String inputPath = part.equals( "left" ) ? removeDuplicatePairsFolder : removeDuplicateLeftFolder;
		String outputPath = part.equals( "left" ) ? removeDuplicateLeftFolder : removeDuplicateRightFolder;
			
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( outputPath ), true );
		
		conf.set( "removePart", part );
		Job job = new Job( conf, "Removing duplicate " + part + " parts: " + info );

		job.setJarByClass( MRMatcher.class );
		job.setMapperClass( RemoveDuplicatePartsMapper.class );
		job.setReducerClass( RemoveDuplicatePartsReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );

		job.waitForCompletion( true );
	}
	
	public static void TransferSameAs() throws Exception
	{
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( transferMatchesFolder ), true );
		
		Job job = new Job( conf, "Transfering <owl:sameAs> relation" );

		job.setJarByClass( MRMatcher.class );
		job.setMapperClass( TransferSameAsMapper.class );
		job.setReducerClass( TransferSameAsReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( allMatchesFolder ) );
		FileOutputFormat.setOutputPath( job, new Path( transferMatchesFolder ) );

		job.waitForCompletion( true );
	}

	public static void main( String[] args ) throws Exception
	{
		Configuration conf = new Configuration();
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( allMatchesFolder ), true );
		fs.mkdirs( new Path( allMatchesFolder ) );
		
		String[] source = { URICenter.source_name_baidu, URICenter.source_name_hudong, URICenter.source_name_zhwiki };
		for( int i = 0; i < 3; ++i )
		{
			String sourceA = source[i];
			String sourceB = source[(i+1)%3];
			String info = sourceA + "-" + sourceB;
			match( sourceA, sourceB );
			RemoveDuplicatePairs( info );
			RemoveDuplicateParts( "left", info );
			RemoveDuplicateParts( "right", info );
			fs.delete( new Path( initialMatchesFolder ), true );
			fs.delete( new Path( removeDuplicatePairsFolder ), true );
			fs.delete( new Path( removeDuplicateLeftFolder ), true );
			String singleMatchFolder = removeDuplicateRightFolder;
			System.out.println( "CopyMerging " + info + "..." );
			FileUtil.copyMerge( fs, new Path( singleMatchFolder ), fs, new Path( allMatchesFolder + info ), true, conf, "" );
		}
		TransferSameAs();
		FileUtil.copyMerge( fs, new Path( transferMatchesFolder ), fs, new Path( allMatchesFolder ), true, conf, "" );
		initialMatchesFolder = allMatchesFolder;
		RemoveDuplicatePairs( "Transfered Matches" );
		fs.delete( new Path( allMatchesFolder ), true );
		System.out.println( "CopyMerging final matches..." );
		FileUtil.copyMerge( fs, new Path( removeDuplicatePairsFolder ), fs, new Path( matchingFile ), true, conf, "" );
	}

}
