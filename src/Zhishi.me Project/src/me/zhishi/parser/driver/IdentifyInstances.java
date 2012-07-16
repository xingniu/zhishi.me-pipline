package me.zhishi.parser.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import me.zhishi.analyzer.InfoboxAnalyzer;
import me.zhishi.parser.tools.TypeNormalize;
import me.zhishi.tools.SmallTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

public class IdentifyInstances
{
	public static double releaseVersion = 3.0;
	private static int numReduceTasks = 10;
	private static HashSet<String> Whitelist = TypeNormalize.List();
	
	public static void main( String[] args ) throws Exception
	{
//		String source = URICenter.source_name_hudong;
		String source = URICenter.source_name_baidu;
//		run( source );
		datatype( source );
	}
	
	public static void run( String source ) throws Exception
	{
		identify( source );
		output( source );
	}
	
	public static class DatatypeStatistics extends Reducer<Object, Text, NullWritable, Text>
	{
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			Pattern TypePatt = Pattern.compile("[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
			Pattern LeadZero = Pattern.compile("0[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
			HashMap<String, Integer> TypeOccur =  new HashMap<String, Integer>();
			LinkedList<String> TypeList = new LinkedList<String>();
			
			for( Text val : values )
			{
				String triple = val.toString();
				TripleReader tr = new TripleReader( triple );
				String oc = tr.getObjectValue();
				oc = TypeNormalize.RemoveParen(oc);
				oc = oc.replaceAll( "[￥]", "" );
				if ( TypePatt.matcher( oc ).matches() && !LeadZero.matcher( oc ).matches() )
				{
					oc = oc.replaceFirst("[0-9]+(\\.[0-9]+)?", "");
					oc = oc.replaceAll("[起余多　左右以上下 .]", "");
					oc = oc.replaceAll("[十百千万兆亿]", "");
					oc = TypeNormalize.Normalize(oc);
//					oc = oc.replaceAll("[人口名个户位学生字]", "");
					if ( Whitelist.contains(oc) )
					{
						if ( TypeOccur.containsKey(oc) )
						{
							TypeOccur.put(oc , TypeOccur.get(oc) + 1);
						}
						else
						{
							TypeList.add(oc);
							TypeOccur.put(oc , 1);
						}
					}
				}
			}
			
			int max = 0;
			String represent = null;
			for( String type : TypeList )
			{
				if ( TypeOccur.get(type) > max )
				{
					max = TypeOccur.get(type);
					represent = type;
				}
//				Text text = new Text( key + " " + type + " : " + TypeOccur.get(type) );
//				context.write( NullWritable.get(), text );
			}
			if ( represent != null )
			{
				Text text = new Text( key + "\t" + represent );
				context.write( NullWritable.get(), text );
			}
		}
	}
	
	public static class IndexByTerms extends Mapper<Object, Text, Text, Text>
	{
		private HashMap<String,String> propUnitMap;
		
		@Override
		protected void setup( Context context ) throws IOException, InterruptedException
		{
			propUnitMap = new HashMap<String, String>();
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get( conf );
			
			Path path = new Path( conf.get( "datatypeStatistics" ) );
			BufferedReader reader = new BufferedReader( new InputStreamReader( fs.open( path ) ) );
			String line;
			while( (line = reader.readLine()) != null )
			{
				String[] segs = line.split( "\t" );
				propUnitMap.put( segs[0], segs[1] );
			}
			reader.close();
		}
		
		@Override
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			TripleReader tr = new TripleReader( value.toString() );
			String pre = tr.getBarePredicate();
			if( pre.equals( URICenter.predicate_rdfs_label ) )
			{
				//label
				context.write( new Text( tr.getObjectValue() ), value );
			}
			else if( pre.equals( URICenter.predicate_redirect ) )
			{
				//redirect
				context.write( new Text( tr.getSubjectContent() ), value );
			}
			else
			{
				//infobox property
				for( String seg : InfoboxAnalyzer.segement( tr.getSubjectContent(), tr.getObjectValue() ) )
				{
					context.write( new Text( seg ), new Text( TripleWriter.getStringValueTriple(tr.getBareSubject(), tr.getBarePredicate(), seg) ) );
				}
			}
			
		}
	}
	
	public static class ValueToURI extends Reducer<Object, Text, NullWritable, Text>
	{
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			LinkedList<String> infoSP = new LinkedList<String>();
			String uri = null;
			for( Text val : values )
			{
				TripleReader tr = new TripleReader( val.toString() );
				String pre = tr.getBarePredicate();
				if( pre.equals( URICenter.predicate_rdfs_label ) )
				{
					uri = tr.getSubject();
				}
				else if( pre.equals( URICenter.predicate_redirect ) )
				{
					uri = tr.getObject();
				}
				else
				{
					infoSP.add( tr.getSubject() + " " + tr.getPredicate() );
				}
			}
			
			if( uri != null )
			{
				for( String triple : infoSP )
				{
					Text text = new Text( triple + " " + uri + " ." );
					context.write( NullWritable.get(), text );
				}
			}
		}
	}
	
	public static class IndexByProperties extends Mapper<Object, Text, Text, Text>
	{
		@Override
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			TripleReader tr = new TripleReader( value.toString() );
			context.write( new Text( tr.getPredicateContent() ), value );
		}
	}
	
	public static class PropertyStatistics extends Reducer<Object, Text, NullWritable, Text>
	{
		private MultipleOutputs<NullWritable, Text> mos;
		
		@Override
		protected void setup( Context context ) throws IOException, InterruptedException
		{
			super.setup( context );
			mos = new MultipleOutputs<NullWritable, Text>( context );
		}
		
		@Override
		public void cleanup( Context context )
		{
			try
			{
				mos.close();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
		
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashSet<String> LiteralObjSet = new HashSet<String>();
			HashSet<String> URIRefObjSet = new HashSet<String>();
			LinkedList<String> LiteralObjList = new LinkedList<String>();
			LinkedList<String> URIRefObjList = new LinkedList<String>();
			for( Text val : values )
			{
				String triple = val.toString();
				TripleReader tr = new TripleReader( triple );
				if( tr.objectIsLiteral() )
				{
					LiteralObjSet.add( tr.getSubjectContent() );
					LiteralObjList.add( triple );
				}
				else if( tr.objectIsURIRef() )
				{
					URIRefObjSet.add( tr.getSubjectContent() );
					URIRefObjList.add( triple );
				}
			}
			
			Text text = new Text( key + " " + URIRefObjSet.size() + "/" + LiteralObjSet.size() );
			mos.write( "statistics", NullWritable.get(), text );
			// precondition
			if( URIRefObjSet.size() > 10000 )
			{
				for( String triple : URIRefObjList )
					context.write( NullWritable.get(), new Text( triple ) );
			}
			else
			{
				for( String triple : LiteralObjList )
					context.write( NullWritable.get(), new Text( triple ) );
			}
		}
	}
	
	public static void datatype( String source ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_DT_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_DT_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Path in = new Path( inputPath );
		fs.mkdirs( in );
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME# Identifying Datatype: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
	
			job.setJarByClass( IdentifyInstances.class );
			job.setMapperClass( IndexByProperties.class );
			job.setReducerClass( DatatypeStatistics.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
	
			if( job.waitForCompletion( true ) )
			{
				SmallTools.moveMergeFiles( fs, "part", outputPath + "statistics", conf, outputPath, numReduceTasks );
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
			fs.delete( in, true );
		}
	}

	public static void identify( String source ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_ID_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_ID_OUT/";
		String dtStatistics = p.getNTriplesFolder() + source + "_DT_OUT/statistics";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Path in = new Path( inputPath );
		fs.mkdirs( in );
		fs.rename( new Path( p.getNTriplesFile( "label" ) ), new Path( inputPath + "label" ) );
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		fs.rename( new Path( p.getNTriplesFile( "redirect" ) ), new Path( inputPath + "redirect" ) );
		
		try
		{
			conf.set( "datatypeStatistics", dtStatistics );
			Job job = new Job( conf, "ZHISHI.ME# Identifying Instances: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
	
			job.setJarByClass( IdentifyInstances.class );
			job.setMapperClass( IndexByTerms.class );
			job.setReducerClass( ValueToURI.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
			
//			for( String s : contents )
//				MultipleOutputs.addNamedOutput( job, s, TextOutputFormat.class, NullWritable.class, Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
	
			if( job.waitForCompletion( true ) )
			{
//				fs.delete( new Path( dtStatistics ), true );
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
			fs.rename( new Path( inputPath + "label" ), new Path( p.getNTriplesFile( "label" ) ) );
			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
			fs.rename( new Path( inputPath + "redirect" ), new Path( p.getNTriplesFile( "redirect" ) ) );
			fs.delete( in, true );
		}
	}
	
	public static void output( String source ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_ID_OUT/";
		String outputPath = p.getNTriplesFolder() + source + "_ST_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME# Property Statistics: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
			
			job.setJarByClass( IdentifyInstances.class );
			job.setMapperClass( IndexByProperties.class );
			job.setReducerClass( PropertyStatistics.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
			
			MultipleOutputs.addNamedOutput( job, "statistics", TextOutputFormat.class, NullWritable.class, Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
			
			if( job.waitForCompletion( true ) )
			{
				System.out.println( "Start moveMerging files ..." );
				SmallTools.moveMergeFiles( fs, "part", p.getNTriplesFile( "infobox" ), conf, outputPath, numReduceTasks );
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
		}
	}
}
