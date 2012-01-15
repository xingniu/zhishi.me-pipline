package me.zhishi.parser.driver;

import java.io.FileNotFoundException;
import java.io.IOException;

import me.zhishi.tools.SmallTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;
import me.zhishi.tools.file.TripleWriter;

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
//	public static String source = URICenter.source_name_baidu;
	public static double releaseVersion = 3.0;
	private static int numReduceTasks = 10;
	
	private static String[] contents = {
//						"label",
//						"category",
//						"abstract",
//						"relatedPage",
//						"internalLink",
//						"externalLink",
//						"redirect",
//						"disambiguation",
//						"articleLink",
						"image",
						"imageInfo",
						"exception",
						};
	
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
				if( tr.getPredicate().equals( URICenter.predicate_rdfs_label ) && tr.getSubject().startsWith( "<" + URICenter.domainName ) )
					;
//					mos.write( "label", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_category ) )
//					mos.write( "category", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_abstract ) )
//					mos.write( "abstract", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_relatedPage ) )
//					mos.write( "relatedPage", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_internalLink ) )
//					mos.write( "internalLink", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_externalLink ) )
//					mos.write( "externalLink", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_redirect ) )
//					mos.write( "redirect", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_disambiguation ) )
//					mos.write( "disambiguation", NullWritable.get(), val );
//				else if( tr.getPredicate().equals( URICenter.predicate_foaf_page ) )
//				{
//					String resource = tr.getSubject();
//					String articleLink = tr.getObject();
//					Text pt = new Text( TripleWriter.getTripleLine( articleLink, URICenter.predicate_foaf_primaryTopic, resource ) );
//					Text lang = new Text( TripleWriter.getTripleLine( articleLink, URICenter.predicate_dc_language, "\"zh\"@en" ) );
//					mos.write( "articleLink", NullWritable.get(), pt );
//					mos.write( "articleLink", NullWritable.get(), lang );
//					mos.write( "articleLink", NullWritable.get(), val );
//				}
				else if( tr.getPredicate().equals( URICenter.predicate_foaf_depiction ) )
					mos.write( "image", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_depictionThumbnail ) )
					mos.write( "image", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_relatedImage ) )
					mos.write( "image", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_rdfs_label ) )
					mos.write( "imageInfo", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_dc_rights ) )
					mos.write( "imageInfo", NullWritable.get(), val );
				else if( tr.getPredicate().equals( URICenter.predicate_foaf_thumbnail ) )
					mos.write( "imageInfo", NullWritable.get(), val );
				else if( tr.getPredicate().equals( "<exception>" ) )
					mos.write( "exception", NullWritable.get(), val );
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
				e.printStackTrace();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void main( String[] args ) throws Exception
	{
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getRawStructuredDataPath();
		String outputPath = p.getNTriplesPath() + source + "/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "sort NTs by predicate: " + source );
		
		job.setNumReduceTasks( numReduceTasks );

		job.setJarByClass( SortByPredicate.class );
		job.setMapperClass( SortByPredicateMapper.class );
		job.setReducerClass( SortByPredicateReducer.class );
		
		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );
		
		for( String s : contents )
			MultipleOutputs.addNamedOutput( job, s, TextOutputFormat.class, NullWritable.class, Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );

		if( job.waitForCompletion( true ) )
		{
			for( String s : contents )
			{
				System.out.println( "Start moving Files: " + s );
				moveMergeFiles( fs, s, p.getFilePath( s ), conf, outputPath );
			}
			fs.delete( new Path( outputPath ), true );
		}
	}
	
	public static void moveMergeFiles( FileSystem fs, String prefix, String target, Configuration conf, String folder ) throws IOException
	{
		String tempFolder = folder + "Temp/";
		Path tempPath = new Path( tempFolder );
		fs.mkdirs( tempPath );
		
		for( int i = 0; i < numReduceTasks; ++ i )
		{
			String fileName = SmallTools.getHadoopOutputName( prefix, i );
			try
			{
				System.out.println( "Copying " + fileName );
				FileUtil.copy( fs, new Path(folder+fileName), fs, new Path(tempFolder+fileName), true, conf );
			}
			catch( FileNotFoundException e )
			{
				System.err.println( folder + fileName + " not found" );
			}
		}
		
		Path targetPath = new Path( target );
		fs.delete( targetPath, true );
		System.out.println( "Merging..." );
		FileUtil.copyMerge( fs, tempPath, fs, targetPath, true, conf, "" );
		fs.delete( tempPath, true );
	}
}
