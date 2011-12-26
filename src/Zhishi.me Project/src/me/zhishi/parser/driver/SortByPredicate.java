package me.zhishi.parser.driver;

import java.io.IOException;

import me.zhishi.tools.TripleReader;
import me.zhishi.tools.URICenter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
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

public class SortByPredicate
{
	public static String source = URICenter.source_name_hudong;
	public static double releaseVersion =3.0;
	
	public static class SortByPredicateMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			TripleReader tr = new TripleReader( value.toString() );
			context.write( new Text( tr.getSubject() ), value );
		}
	}
	
	public static class SortByPredicateReducer extends Reducer<Object, Text, NullWritable, Text>
	{
		private MultipleOutputs<NullWritable, Text> mos;
		
		@Override
		protected void setup( Context context ) throws IOException, InterruptedException
		{
			super.setup( context );
			mos = new MultipleOutputs<NullWritable, Text>( context );
		}

		@Override
		public void reduce( Object key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			for( Text val : values )
			{
				TripleReader tr = new TripleReader( val.toString() );
				if( tr.getPredicate().equals( URICenter.predicate_label ) )
					mos.write( "label", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_article_category ) )
					mos.write( "category", NullWritable.get(), val );
			}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( InterruptedException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main( String[] args ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getHDFSRawStructuredDataPath();
		String outputPath = p.getNTriplesPath() + "Temp/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
//		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "sort NTs by predicate: " + source );
		
		job.setNumReduceTasks( 1 );

		job.setJarByClass( SortByPredicate.class );
		job.setMapperClass( SortByPredicateMapper.class );
		job.setReducerClass( SortByPredicateReducer.class );
		
		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );
		
		MultipleOutputs.addNamedOutput( job, "label", TextOutputFormat.class, NullWritable.class, Text.class );
		MultipleOutputs.addNamedOutput( job, "category", TextOutputFormat.class, NullWritable.class, Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );

		if( job.waitForCompletion( true ) )
		{
			FileUtil.copy( fs, new Path(outputPath+"label-r-00000"), fs, new Path(p.getLabelFileName()), false, conf );
			FileUtil.copy( fs, new Path(outputPath+"category-r-00000"), fs, new Path(p.getCategoryFileName()), false, conf );
			fs.delete( new Path( outputPath ), true );
		}
	}
}
