package me.zhishi.parser.workshop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

public class InfoboxRefiner
{
	public static int maxUnitLength = 3;
	private static int numReduceTasks = 10;
	
	public static void run( String source, double releaseVersion ) throws Exception
	{
		datatype( source, releaseVersion );
		identify( source, releaseVersion );
		output( source, releaseVersion );
	}
	
	public static class DatatypeStatistics extends Reducer<Object, Text, NullWritable, Text>
	{
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashMap<String, Integer> TypeOccur = new HashMap<String, Integer>();
			LinkedList<String> TypeList = new LinkedList<String>();
			boolean isDouble = false;
			
			for( Text val : values )
			{
				String triple = val.toString();
				TripleReader tr = new TripleReader( triple );
				String obj = tr.getObjectValue();

				String unit = TypeNormalize.CheckType( obj );

				if( unit != null )
				{
					String datavalue = TypeNormalize.getValue( obj );
					if( datavalue.contains( "." ) )
						isDouble = true;
					if( TypeOccur.containsKey( unit ) )
					{
						TypeOccur.put( unit, TypeOccur.get( unit ) + 1 );
					}
					else
					{
						TypeList.add( unit );
						TypeOccur.put( unit, 1 );
					}
				}
			}

			int max = 0;
			String represent = null;
			for( String type : TypeList )
			{
				if( TypeOccur.get( type ) > max )
				{
					max = TypeOccur.get( type );
					represent = type;
				}
			}
			if( represent != null )
			{
				if( !TypeNormalize.List.contains( represent ) )
					represent = "";
				Text text = new Text( key + "\t" + represent );
				context.write( NullWritable.get(), text );
				text = new Text( key + "\t" + (isDouble ? "Double" : "Int") );
				context.write( NullWritable.get(), text );
			}
		}
	}
	
	public static class IndexByTerms extends Mapper<Object, Text, Text, Text>
	{
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
		private HashMap<String,String> propUnitMap;
		private HashMap<String,String> propValueType;
		
		@Override
		protected void setup( Context context ) throws IOException, InterruptedException
		{
			propUnitMap = new HashMap<String, String>();
			propValueType = new HashMap<String, String>();
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get( conf );
			
			Path path = new Path( conf.get( "datatypeStatistics" ) );
			BufferedReader reader = new BufferedReader( new InputStreamReader( fs.open( path ) ) );
			String line;
			while( (line = reader.readLine()) != null )
			{
				String[] segs = line.split( "\t" );
				if ( segs.length == 2 )	propUnitMap.put( segs[0], segs[1] );
				else
				{
					propUnitMap.put( segs[0], "" );
				}
				
				line = reader.readLine();
				segs = line.split( "\t" );
				propValueType.put( segs[0], segs[1] );
			}
			reader.close();
		}
		
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
					
					String preContent = tr.getPredicateContent();
					if ( propUnitMap.containsKey(preContent) )
					{
						boolean hasUnit = false;
						String dataValue = "";
						String valueType = "";
						
						//get unit
						String unit = TypeNormalize.CheckType( key.toString() );
						if ( unit != null )
						{
							if ( unit == propUnitMap.get(preContent) || 
								 TypeNormalize.Table.containsKey(unit + " " + propUnitMap.get(preContent)) )
								hasUnit = true;
							else if ( unit.length() <= maxUnitLength )
							{
								unit = propUnitMap.get(preContent);
								hasUnit = true;
							}
						}
						
						//date data
						if ( TypeNormalize.isDate( key.toString() ) )
						{
							unit = "";
							valueType = URICenter.datatype_xmls_date;
							dataValue = key.toString();
							
							Text text = new Text( TripleWriter.getValueTriple( tr.getBareSubject(), tr.getBarePredicate(), dataValue, valueType ) );
							context.write( NullWritable.get(), text );
							text = new Text( TripleWriter.getStringValueTriple( tr.getBarePredicate(), URICenter.predicate_temp_unit, unit ) );
							context.write( NullWritable.get(), text );
						}
						
						//other data
						if ( hasUnit )
						{
							//get value type
							if ( propValueType.get(preContent) == "Int" ) valueType = URICenter.datatype_xmls_int;
							else valueType = URICenter.datatype_xmls_double;
							
							//get data value and normalize the unit
							dataValue = TypeNormalize.getValue( key.toString() );
							if ( unit != propUnitMap.get(preContent) )
							{
								dataValue = TypeNormalize.unitTrans( dataValue, unit, propUnitMap.get(preContent), propValueType.get(preContent) );
								unit = propUnitMap.get(preContent);
							}
							
							Text text = new Text( TripleWriter.getValueTriple( tr.getBareSubject(), tr.getBarePredicate(), dataValue, valueType ) );
							context.write( NullWritable.get(), text );
							text = new Text( TripleWriter.getStringValueTriple( tr.getBarePredicate(), URICenter.predicate_temp_unit, unit ) );
							context.write( NullWritable.get(), text );
						}
					}
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
			if( tr.getBarePredicate().equals( URICenter.predicate_temp_unit ) )
				context.write( new Text( tr.getSubjectContent() ), value );
			else
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
			boolean isString = false;
			
			if( key.toString().endsWith( "名" ) || key.toString().endsWith( "称" ) )
			{
				isString = true;
			}
			
			HashSet<String> StringObjSet = new HashSet<String>();
			HashSet<String> TypedDataObjSet = new HashSet<String>();
			HashSet<String> URIRefObjSet = new HashSet<String>();
			LinkedList<String> StringObjList = new LinkedList<String>();
			LinkedList<String> TypedDataObjList = new LinkedList<String>();
			LinkedList<String> URIRefObjList = new LinkedList<String>();
			String unit = null;
			String property = null;
			for( Text val : values )
			{
				String triple = val.toString();
				TripleReader tr = new TripleReader( triple );
				if( tr.getBarePredicate().equals( URICenter.predicate_temp_unit ) )
				{
					unit = tr.getObjectValue();
					continue;
				}
				else
				{
					property = tr.getBarePredicate();
				}
				
				if( tr.objectIsURIRef() && !isString )
				{
					URIRefObjSet.add( tr.getSubjectContent() );
					URIRefObjList.add( triple );
				}
				else if( tr.objectIsTypedData() && !isString )
				{
					TypedDataObjSet.add( tr.getSubjectContent() );
					TypedDataObjList.add( triple );
				}
				else
				{
					StringObjSet.add( tr.getSubjectContent() );
					StringObjList.add( triple );
				}
			}
			
			Text text = new Text( key + " " + URIRefObjSet.size() + "|" + TypedDataObjSet.size() + "|" + StringObjSet.size() );
			// precondition
			if( TypedDataObjSet.size() >= URIRefObjSet.size() && TypedDataObjSet.size() * 1.0 / StringObjSet.size() >= 0.3 )
			{
				for( String triple : TypedDataObjList )
					context.write( NullWritable.get(), new Text( triple ) );
				if( !unit.equals( "" ) )
				{
					String unitTriple = TripleWriter.getStringValueTriple( property, URICenter.predicate_prefUnit, unit );
					mos.write( "info", NullWritable.get(), new Text( unitTriple ) );
				}
				mos.write( "statistics", NullWritable.get(), new Text( text + " TypedData" ) );
			}
			else if( ( URIRefObjSet.size() * 1.0 / StringObjSet.size() >= 0.3 && URIRefObjSet.size() >= 10 )
					|| URIRefObjSet.size() * 1.0 / StringObjSet.size() >= 0.7
					|| ( URIRefObjSet.size() > TypedDataObjSet.size() && URIRefObjSet.size() >= 1000 ) )
			{
				for( String triple : URIRefObjList )
					context.write( NullWritable.get(), new Text( triple ) );
				mos.write( "statistics", NullWritable.get(), new Text( text + " URIRef" ) );
			}
			else
			{
				for( String triple : StringObjList )
					context.write( NullWritable.get(), new Text( triple ) );
				mos.write( "statistics", NullWritable.get(), new Text( text + " String" ) );
			}
			
			String labelTriple = TripleWriter.getStringValueTriple( property, URICenter.predicate_rdfs_label, key.toString() );
			mos.write( "info", NullWritable.get(), new Text( labelTriple ) );
		}
	}
	
	public static void datatype( String source, double releaseVersion ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_DT_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_DT_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Path in = new Path( inputPath );
		fs.mkdirs( in );
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME# Identifying Datatype: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
	
			job.setJarByClass( InfoboxRefiner.class );
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

	public static void identify( String source, double releaseVersion ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_ID_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_ID_OUT/";
		String dtStatistics = p.getNTriplesFolder() + source + "_DT_OUT/statistics";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
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
	
			job.setJarByClass( InfoboxRefiner.class );
			job.setMapperClass( IndexByTerms.class );
			job.setReducerClass( ValueToURI.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
	
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
	
	public static void output( String source, double releaseVersion ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_ID_OUT/";
		String outputPath = p.getNTriplesFolder() + source + "_ST_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME# Property Statistics: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
			
			job.setJarByClass( InfoboxRefiner.class );
			job.setMapperClass( IndexByProperties.class );
			job.setReducerClass( PropertyStatistics.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
			
			MultipleOutputs.addNamedOutput( job, "statistics", TextOutputFormat.class, NullWritable.class, Text.class );
			MultipleOutputs.addNamedOutput( job, "info", TextOutputFormat.class, NullWritable.class, Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
			
			if( job.waitForCompletion( true ) )
			{
				System.out.println( "Start moveMerging files ..." );
				SmallTools.moveMergeFiles( fs, "part", p.getNTriplesFile( "infobox" ), conf, outputPath, numReduceTasks );
				SmallTools.moveMergeFiles( fs, "info", p.getNTriplesFile( "propertyDefinition" ), conf, outputPath, numReduceTasks );
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
		}
	}
}
