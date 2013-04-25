package me.zhishi.parser.workshop;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

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

public class PropertyRestriction
{
	private static int numReduceTasks = 10;
	private static String dt = "[DataType]";
	
	public static void run( String source, double releaseVersion ) throws Exception
	{
		rawData( source, releaseVersion );
		output( source, releaseVersion );
	}
	
	public static class IndexByResources extends Mapper<Object, Text, Text, Text>
	{
		@Override
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			TripleReader tr = new TripleReader( value.toString() );
			if( tr.getBarePredicate().equals( URICenter.predicate_category ) )
				context.write( new Text( tr.getSubjectContent() ), new Text( tr.getBareObject() ) );
			else
			{
				context.write( new Text( tr.getSubjectContent() ), new Text( "domain\t" + tr.getBarePredicate() ) );
				
				if( tr.objectIsURIRef() )
					context.write( new Text( tr.getObjectContent() ), new Text( "range\t" + tr.getBarePredicate() ) );
				else if( tr.objectIsTypedData() )
					context.write( new Text( dt ), new Text( tr.getBarePredicate() + "\trange\t" + tr.getDataType() ) );
				else
					context.write( new Text( dt ), new Text( tr.getBarePredicate() + "\trange\t" + URICenter.datatype_xmls_string ) );
			}
		}
	}
	
	public static class PropertyRawData extends Reducer<Object, Text, NullWritable, Text>
	{
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			if( key.toString().equals( dt ) )
			{
				for( Text val : values )
				{
					context.write( NullWritable.get(), val );
				}
				return;
			}
			
			HashSet<String> domainProperties = new HashSet<String>();
			HashSet<String> rangeProperties = new HashSet<String>();
			HashSet<String> categories = new HashSet<String>();
			for( Text val : values )
			{
				String[] segs = val.toString().split( "\t" );
				if( segs.length == 1 )
					categories.add( segs[0] );
				else if( segs[0].equals( "domain" ) )
					domainProperties.add( segs[1] );
				else if( segs[0].equals( "range" ) )
					rangeProperties.add( segs[1] );
			}
			
			for( String dp : domainProperties )
			{
				for( String cat : categories )
				{
					context.write( NullWritable.get(), new Text( dp + "\tdomain\t" + cat ) );
				}
			}
			for( String rp : rangeProperties )
			{
				for( String cat : categories )
				{
					context.write( NullWritable.get(), new Text( rp + "\trange\t" + cat ) );
				}
			}
		}
	}
	
	public static class IndexByProperties extends Mapper<Object, Text, Text, Text>
	{
		@Override
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String[] segs = value.toString().split( "\t" );
			context.write( new Text( segs[0] + "\t" + segs[1] ), new Text( segs[2] ) );
		}
	}
	
	public static class CategoryStatistics extends Reducer<Object, Text, NullWritable, Text>
	{
		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashMap<String,Integer> counter = new HashMap<String,Integer>();
			for( Text val : values )
			{
				String cat = val.toString();
				if( counter.containsKey( cat ) )
					counter.put( cat, counter.get( cat ) + 1 );
				else
					counter.put( cat, 1 );
			}
			
			int max = 0;
			String category = null;
			for( Entry<String,Integer> pair : counter.entrySet() )
			{
				if( pair.getValue() > max )
				{
					max = pair.getValue();
					category = pair.getKey();
				}
			}
			
			String[] segs = key.toString().split( "\t" );
			String predicate = segs[1].equals( "domain" ) ? URICenter.predicate_rdfs_domain : URICenter.predicate_rdfs_range;
			
//			context.write( NullWritable.get(), new Text( TripleWriter.getResourceObjectTriple( segs[0], predicate, category ) ) );
			
			String p = URICenter.zhishiDecode( segs[0].substring( segs[0].lastIndexOf( "/" )+1, segs[0].length() ) );
			String c = URICenter.zhishiDecode( category.substring( category.lastIndexOf( "/" )+1, category.length() ) );
			context.write( NullWritable.get(), new Text( TripleWriter.getResourceObjectTriple( p, predicate, c ) ) );
		}
	}
	
	public static void rawData( String source, double releaseVersion ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_Property_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_Property_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Path in = new Path( inputPath );
		fs.mkdirs( in );
		fs.rename( new Path( p.getNTriplesFile( "infobox" ) ), new Path( inputPath + "infobox" ) );
		fs.rename( new Path( p.getNTriplesFile( "category" ) ), new Path( inputPath + "category" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME# Getting Property Restrictions (1): " + source );
			
			job.setNumReduceTasks( numReduceTasks );
	
			job.setJarByClass( PropertyRestriction.class );
			job.setMapperClass( IndexByResources.class );
			job.setReducerClass( PropertyRawData.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
	
			job.waitForCompletion( true );
		}
		finally
		{
			fs.rename( new Path( inputPath + "infobox" ), new Path( p.getNTriplesFile( "infobox" ) ) );
			fs.rename( new Path( inputPath + "category" ), new Path( p.getNTriplesFile( "category" ) ) );
			fs.delete( in, true );
		}
	}
	
	public static void output( String source, double releaseVersion ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_Property_OUT/";
		String outputPath = p.getNTriplesFolder() + source + "_Restriction_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.defaultFS", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "ZHISHI.ME# Getting Property Restrictions (2): " + source );
		
		job.setNumReduceTasks( numReduceTasks );

		job.setJarByClass( PropertyRestriction.class );
		job.setMapperClass( IndexByProperties.class );
		job.setReducerClass( CategoryStatistics.class );
		
		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
		
//		Path in = new Path( inputPath );
//		fs.mkdirs( in );
//		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			if( job.waitForCompletion( true ) )
			{
				fs.delete( new Path( inputPath ), true );
//				SmallTools.moveMergeFiles( fs, "part", outputPath + "statistics", conf, outputPath, numReduceTasks );
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
//			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
		}
	}
}
