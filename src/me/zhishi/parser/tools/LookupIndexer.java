package me.zhishi.parser.tools;

import java.io.IOException;
import java.util.HashSet;

import me.zhishi.tools.TextTools;
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

public class LookupIndexer
{
	public static double releaseVersion = 3.0;
	
	public static class LookupIndexerMapper extends Mapper<Object, Text, Text, Text>
	{
		private void writeWithoutPunctuations( String key, String value, Context context ) throws IOException, InterruptedException
		{
			key = key.trim();
			if( key.equals( "" ) )
				return;
			String tmpKey = key.replaceAll( TextTools.ignorablePunctuations, "" );
			if( !tmpKey.equals( key ) )
				context.write( new Text( tmpKey ), new Text( value ) );
			context.write( new Text( key ), new Text( value ) );
		}
		
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String line = value.toString();
			if( line.startsWith( "<" ) )
			{
				TripleReader tr = new TripleReader( line );
				if( tr.getPredicate().equals( URICenter.predicate_rdfs_label ) )
				{
					writeWithoutPunctuations( tr.getObjectValue(), tr.getSubject(), context );
				}
				else if( tr.getPredicate().equals( URICenter.predicate_redirect ) )
				{
					writeWithoutPunctuations( tr.getObjectContent(), tr.getSubject(), context );
					writeWithoutPunctuations( tr.getSubjectContent(), tr.getObject(), context );
				}
				else if( tr.getPredicate().equals( URICenter.predicate_disambiguation ) )
				{
					writeWithoutPunctuations( tr.getSubjectContent(), tr.getObject(), context );
				}
			}
			else
			{
				String segs[] = line.split( "\t" );
				String labelA = URICenter.getURIContent( segs[1] );
				String labelB = URICenter.getURIContent( segs[2] );
				if( !labelA.equals( labelB ) )
				{
					writeWithoutPunctuations( labelA, segs[2], context );
					writeWithoutPunctuations( labelB, segs[1], context );
				}
			}
		}
	}

	public static class LookupIndexerReducer extends Reducer<Text, Text, NullWritable, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashSet<String> resourceSet = new HashSet<String>();
			for( Text val : values )
			{
				resourceSet.add( val.toString() ); 
			}
			for( String res : resourceSet )
			{
				String index = "<" + URICenter.zhishiEncode( key.toString() ) + ">";
				context.write( NullWritable.get(), new Text( TripleWriter.getTripleLine( index, URICenter.predicate_temp_lookup, res ) ) );
			}
		}
	}

	public static void main( String[] args ) throws Exception
	{
		String inputPath = me.zhishi.tools.Path.hdfs_projectDataPath + "LookupInput/";
		String outPath = me.zhishi.tools.Path.hdfs_projectDataPath + "LookupOut/";
		
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, true );
		
		Configuration conf = new Configuration();
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( inputPath ), true );
		FileUtil.copy( fs, new Path( p.getMatchingFile() ), fs, new Path( inputPath + "matches" ), false, conf );
		p.setSource( URICenter.source_name_baidu );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "label" ) ), fs, new Path( inputPath + "baiduLabel" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "redirect" ) ), fs, new Path( inputPath + "baiduRedirect" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "disambiguation" ) ), fs, new Path( inputPath + "baiduDis" ), false, conf );
		p.setSource( URICenter.source_name_hudong );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "label" ) ), fs, new Path( inputPath + "hudongLabel" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "redirect" ) ), fs, new Path( inputPath + "hudongRedirect" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "disambiguation" ) ), fs, new Path( inputPath + "hudongDis" ), false, conf );
		p.setSource( URICenter.source_name_zhwiki );
		// TODO: version 2.0
		p.setReleaseVersion( 2.0 );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "label" ) ), fs, new Path( inputPath + "zhwikiLabel" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "redirect" ) ), fs, new Path( inputPath + "zhwikiRedirect" ), false, conf );
		FileUtil.copy( fs, new Path( p.getNTriplesFile( "disambiguation" ) ), fs, new Path( inputPath + "zhwikiDis" ), false, conf );
		fs.delete( new Path( outPath ), true );
		
		Job job = new Job( conf, "Generate lookup index" );
		
		job.setNumReduceTasks( 10 );

		job.setJarByClass( LookupIndexer.class );
		job.setMapperClass( LookupIndexerMapper.class );
		job.setReducerClass( LookupIndexerReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outPath ) );

		if( job.waitForCompletion( true ) )
		{
			System.out.println( "Merging..." );
			// TODO: version 2.9
			p.setReleaseVersion( 2.9 );
			FileUtil.copyMerge( fs, new Path( outPath ), fs, new Path( p.getNTriplesFile( "lookup" ) ), true, conf, "" );
			fs.delete( new Path( inputPath ), true );
		}
	}

}
