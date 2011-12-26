package me.zhishi.tools;

import java.io.IOException;
import java.util.HashSet;

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
import org.json.JSONException;
import org.json.JSONObject;

public class NTriplesToJSON
{
	private static HashSet<String> predicateSet;
	
	public static class NTriplesToJSONMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			predicateSet = new HashSet<String>();
			predicateSet.add( "label" );
			predicateSet.add( "category" );
			
			try
			{
				TripleReader tr = new TripleReader( value.toString() );
				
				String source = null;
				if( tr.getSubject().contains( URICenter.namespace_baidu ) )
				{
					source = URICenter.source_name_baidu;
				}
				else if( tr.getSubject().contains( URICenter.namespace_hudong ) )
				{
					source = URICenter.source_name_hudong;
				}
				else if( tr.getSubject().contains( URICenter.namespace_zhwiki ) )
				{
					source = URICenter.source_name_zhwiki;
				}
				
				if( predicateSet.contains( "label" ) && tr.getPredicate().equals( URICenter.predicate_label ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "label" );
					main.put( "value", tr.getObjectValue() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "category" ) && tr.getPredicate().equals( URICenter.predicate_article_category_old ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "category" );
					main.put( "value", tr.getObjectContent() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				
//				if( tr.getPredicate().endsWith( "resourceID>" ) )
//				{
//					String label = tr.getDecodedSubject();
//					
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "label" );
//					main.put( "value", label );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//					
//					label = label.replaceAll( GlobalConfigs.punctuations, "" );
//					if( !label.equals( tr.getDecodedSubject() ) )
//					{
//						main = new JSONObject();
//						main.put( "uri", tr.getSubject() );
//						main.put( "source", source );
//						main.put( "property", "alias" );
//						main.put( "value", label );
//						context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//					}
//				}
//				else if( tr.getPredicate().endsWith( "pageRedirects>" ) )
//				{
//					String alias = tr.getDecodedSubject();
//					
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getObject() );
//					main.put( "source", source );
//					main.put( "property", "alias" );
//					main.put( "value", alias );
//					context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
//					
//					alias = alias.replaceAll( GlobalConfigs.punctuations, "" );
//					if( !alias.equals( tr.getDecodedSubject() ) )
//					{
//						main = new JSONObject();
//						main.put( "uri", tr.getObject() );
//						main.put( "source", source );
//						main.put( "property", "alias" );
//						main.put( "value", alias );
//						context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
//					}
//					
//					main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "pageRedirect" );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().endsWith( "pageDisambiguates>" ) )
//				{
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "isDisambiguation" );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//					main = new JSONObject();
//					main.put( "uri", tr.getObject() );
//					main.put( "source", source );
//					main.put( "property", "isSynonym" );
//					context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().endsWith( "abstract>" ) )
//				{
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "abstract" );
//					main.put( "value", tr.getBareObject() );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().contains( "/property/" ) )
//				{
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "infoboxValue" );
//					main.put( "value", tr.getBareObject() );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().endsWith( "internalLink>" ) )
//				{
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "internalLink" );
//					main.put( "value", tr.getDecodedObject() );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().endsWith( "relatedPage>" ) )
//				{
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "relatedPage" );
//					main.put( "value", tr.getDecodedObject() );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
//				else if( tr.getPredicate().endsWith( "featureTags>" ) )
//				{
//					String tags = tr.getBareObject();
//					HashSet<String> featureTags = new HashSet<String>( Arrays.asList( tags.split( " " ) ) );
//					JSONObject main = new JSONObject();
//					main.put( "uri", tr.getSubject() );
//					main.put( "source", source );
//					main.put( "property", "featureTags" );
//					main.put( "value", featureTags );
//					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
//				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
	}

	public static class NTriplesToJSONReducer extends Reducer<Text, Text, NullWritable, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			try
			{
				JSONObject main = new JSONObject();
				JSONObject attr = new JSONObject();
//				boolean hasAlias = false;
				boolean hasCategory = false;
//				boolean hasInfoboxValue = false;
//				boolean hasRelatedLemma = false;
//				HashSet<String> aliasSet = new HashSet<String>();
				HashSet<String> categorySet = new HashSet<String>();
//				HashSet<String> infoboxValueSet = new HashSet<String>();
//				HashSet<String> relatedLemmaSet = new HashSet<String>();
				for( Text val : values )
				{
					JSONObject j = new JSONObject( val.toString() );
					if( j.getString( "property" ).equals( "label" ) )
					{
						attr.put( "label", j.getString( "value" ) );
						main.put( "uri", j.getString( "uri" ) );
						main.put( "source", j.getString( "source" ) );
					}
//					else if( j.getString( "property" ).equals( "alias" ) )
//					{
//						hasAlias = true;
//						aliasSet.add( j.getString( "value" ) );
//					}
					else if( j.getString( "property" ).equals( "category" ) )
					{
						hasCategory = true;
						categorySet.add( j.getString( "value" ) );
					}
//					else if( j.getString( "property" ).equals( "infoboxValue" ) )
//					{
//						hasInfoboxValue = true;
//						infoboxValueSet.add( j.getString( "value" ) );
//					}
//					else if( j.getString( "property" ).equals( "internalLink" ) )
//					{
//						hasRelatedLemma = true;
//						relatedLemmaSet.add( j.getString( "value" ) );
//					}
//					else if( j.getString( "property" ).equals( "relatedPage" ) )
//					{
//						hasRelatedLemma = true;
//						relatedLemmaSet.add( j.getString( "value" ) );
//					}
//					else if( j.getString( "property" ).equals( "abstract" ) )
//					{
//						attr.put( "abstract", j.getString( "value" ) );
//					}
//					else if( j.getString( "property" ).equals( "isDisambiguation" ) )
//					{
//						attr.put( "isDisambiguation", "" );
//					}
//					else if( j.getString( "property" ).equals( "isSynonym" ) )
//					{
//						attr.put( "isSynonym", "" );
//					}
//					else if( j.getString( "property" ).equals( "pageRedirect" ) )
//					{
//						attr.put( "pageRedirect", "" );
//					}
				}
//				if( hasAlias )
//				{
//					attr.put( "aliases", aliasSet );
//				}
				if( hasCategory )
				{
					attr.put( "categories", categorySet );
				}
//				if( hasInfoboxValue )
//				{
//					attr.put( "infoboxValues", infoboxValueSet );
//				}
//				if( hasRelatedLemma )
//				{
//					attr.put( "relatedLemmas", relatedLemmaSet );
//				}
				main.put( "attr", attr );

				context.write( NullWritable.get(), new Text( main.toString() ) );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
	}

	public static void main( String[] args ) throws Exception
	{
		String inputPath = "/Users/xingniu/CLOD/NTriples";
		String outputPath = "/Users/xingniu/CLOD/Temp";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "baike NTriple to JSON format" );
		
		job.setNumReduceTasks( 40 );

		job.setJarByClass( NTriplesToJSON.class );
		job.setMapperClass( NTriplesToJSONMapper.class );
		job.setReducerClass( NTriplesToJSONReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );

		System.exit( job.waitForCompletion( true ) ? 0 : 1 );
	}
}
