package me.zhishi.parser.driver;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import me.zhishi.parser.driver.SortByPredicate.SortByPredicateMapper;
import me.zhishi.parser.driver.SortByPredicate.SortByPredicateReducer;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;

public class IdentifyInstances
{
	public static double releaseVersion = 3.0;
	private static int numReduceTasks = 20;
	
	public static void main( String[] args ) throws Exception
	{
		run( URICenter.source_name_baidu );
//		run( URICenter.source_name_hudong );
	}
	
	public static class IndexByTerms extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			TripleReader tr = new TripleReader( value.toString() );
			String text = tr.getObjectValue();
			if( tr.getBarePredicate().equals( URICenter.predicate_rdfs_label ) )
			{
				//label
				context.write( new Text( text ), value );
			}
			else
			{
				//infobox property
				context.write( new Text( text ), value );
			}
			
		}
	}

	public static void run( String source ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getNTriplesFolder() + source + "_IN/";
		String outputPath = p.getNTriplesFolder() + source + "_OUT/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Path in = new Path( inputPath );
		fs.mkdirs( in );
		fs.rename( new Path( p.getNTriplesFile( "label" ) ), new Path( inputPath + "label" ) );
		fs.rename( new Path( p.getNTriplesFile( "infoboxText" ) ), new Path( inputPath + "infoboxText" ) );
		
		try
		{
			Job job = new Job( conf, "ZHISHI.ME#Identifying Instances: " + source );
			
			job.setNumReduceTasks( numReduceTasks );
	
			job.setJarByClass( IdentifyInstances.class );
			job.setMapperClass( IndexByTerms.class );
//			job.setReducerClass( SortByPredicateReducer.class );
			
			job.setOutputKeyClass( Text.class );
			job.setOutputValueClass( Text.class );
			
//			for( String s : contents )
//				MultipleOutputs.addNamedOutput( job, s, TextOutputFormat.class, NullWritable.class, Text.class );
	
			FileInputFormat.addInputPath( job, new Path( inputPath ) );
			FileOutputFormat.setOutputPath( job, new Path( outputPath ) );
	
			if( job.waitForCompletion( true ) )
			{
//				fs.delete( new Path( outputPath ), true );
			}
		}
		finally
		{
			fs.rename( new Path( inputPath + "label" ), new Path( p.getNTriplesFile( "label" ) ) );
			fs.rename( new Path( inputPath + "infoboxText" ), new Path( p.getNTriplesFile( "infoboxText" ) ) );
			fs.delete( in, true );
		}
	}
}
