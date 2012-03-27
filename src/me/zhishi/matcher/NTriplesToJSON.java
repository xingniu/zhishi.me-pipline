package me.zhishi.matcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import me.zhishi.tools.TextTools;
import me.zhishi.tools.URICenter;
import me.zhishi.tools.file.TripleReader;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NTriplesToJSON
{
	public static double releaseVersion = 3.0;
	private static int numReduceTasks = 20;
	private static HashSet<String> predicateSet = new HashSet<String>();
	static
	{
		predicateSet.add( "label" );
		predicateSet.add( "category" );
		predicateSet.add( "abstract" );
		predicateSet.add( "relatedPage" );
		predicateSet.add( "internalLink" );
		predicateSet.add( "infoboxValue" );
		predicateSet.add( "disambiguation" );
		predicateSet.add( "redirect" );
		predicateSet.add( "featureTags" );
	}
	
	public static class NTriplesToJSONMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
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
				
				String pre = tr.getBarePredicate();
				
				if( predicateSet.contains( "label" ) && pre.equals( URICenter.predicate_rdfs_label ) && tr.getBareSubject().startsWith( URICenter.domainName ) )
				{
					String label = tr.getObjectValue();
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "label" );
					main.put( "value", label );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
					
					label = label.replaceAll( TextTools.ignorablePunctuations, "" );
					if( !label.equals( tr.getObjectValue() ) )
					{
						main = new JSONObject();
						main.put( "uri", tr.getSubject() );
						main.put( "source", source );
						main.put( "property", "alias" );
						main.put( "value", label );
						context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
					}
				}
				// TODO: predicate_category_old
				else if( predicateSet.contains( "category" ) && ( pre.equals( URICenter.predicate_category ) || pre.equals( URICenter.predicate_category_old ) ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "category" );
					main.put( "value", tr.getObjectContent() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "redirect" ) && pre.equals( URICenter.predicate_redirect ) )
				{
					String alias = tr.getSubjectContent();
					
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getObject() );
					main.put( "source", source );
					main.put( "property", "alias" );
					main.put( "value", alias );
					context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
					
					alias = alias.replaceAll( TextTools.ignorablePunctuations, "" );
					if( !alias.equals( tr.getSubjectContent() ) )
					{
						main = new JSONObject();
						main.put( "uri", tr.getObject() );
						main.put( "source", source );
						main.put( "property", "alias" );
						main.put( "value", alias );
						context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
					}
					
					main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "pageRedirect" );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "disambiguation" ) && pre.equals( URICenter.predicate_disambiguation ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "isDisambiguation" );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
					main = new JSONObject();
					main.put( "uri", tr.getObject() );
					main.put( "source", source );
					main.put( "property", "isSynonym" );
					context.write( new Text( tr.getObject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "category" ) && pre.equals( URICenter.predicate_abstract ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "abstract" );
					main.put( "value", tr.getObjectValue() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "infoboxValue" ) && pre.matches( "http://zhishi.me/.*/property/.*" ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "infoboxValue" );
					main.put( "value", tr.getObjectValue() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "internalLink" ) && pre.equals( URICenter.predicate_internalLink ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "internalLink" );
					main.put( "value", tr.getObjectContent() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "relatedPage" ) && pre.equals( URICenter.predicate_relatedPage ) )
				{
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "relatedPage" );
					main.put( "value", tr.getObjectContent() );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
				else if( predicateSet.contains( "featureTags" ) && pre.equals( URICenter.predicate_temp_featureTags ) )
				{
					String tags = tr.getObjectValue();
					HashSet<String> featureTags = new HashSet<String>( Arrays.asList( tags.split( " " ) ) );
					JSONObject main = new JSONObject();
					main.put( "uri", tr.getSubject() );
					main.put( "source", source );
					main.put( "property", "featureTags" );
					main.put( "value", featureTags );
					context.write( new Text( tr.getSubject() ), new Text( main.toString() ) );
				}
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
				HashSet<String> aliasSet = new HashSet<String>();
				HashSet<String> categorySet = new HashSet<String>();
				HashSet<String> featureTagSet = new HashSet<String>();
				HashSet<String> infoboxValueSet = new HashSet<String>();
				HashSet<String> relatedLemmaSet = new HashSet<String>();
				for( Text val : values )
				{
					JSONObject j = new JSONObject( val.toString() );
					if( j.getString( "property" ).equals( "label" ) )
					{
						attr.put( "label", j.getString( "value" ) );
						main.put( "uri", j.getString( "uri" ) );
						main.put( "source", j.getString( "source" ) );
					}
					else if( j.getString( "property" ).equals( "alias" ) )
					{
						aliasSet.add( j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "category" ) )
					{
						categorySet.add( j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "infoboxValue" ) )
					{
						infoboxValueSet.add( j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "internalLink" ) )
					{
						relatedLemmaSet.add( j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "relatedPage" ) )
					{
						relatedLemmaSet.add( j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "featureTags" ) )
					{
						JSONArray tags = j.getJSONArray( "value" );
						for( int i = 0; i < tags.length(); ++i )
						{
							featureTagSet.add( tags.getString( i ) );
						}
					}
					else if( j.getString( "property" ).equals( "abstract" ) )
					{
						attr.put( "abstract", j.getString( "value" ) );
					}
					else if( j.getString( "property" ).equals( "isDisambiguation" ) )
					{
						attr.put( "isDisambiguation", "" );
					}
					else if( j.getString( "property" ).equals( "isSynonym" ) )
					{
						attr.put( "isSynonym", "" );
					}
					else if( j.getString( "property" ).equals( "pageRedirect" ) )
					{
						attr.put( "pageRedirect", "" );
					}
				}
				if( aliasSet.size() > 0 )
				{
					attr.put( "aliases", aliasSet );
				}
				if( categorySet.size() > 0 )
				{
					attr.put( "categories", categorySet );
				}
				if( featureTagSet.size() > 0 )
				{
					attr.put( "featureTags", featureTagSet );
				}
				if( infoboxValueSet.size() > 0 )
				{
					attr.put( "infoboxValues", infoboxValueSet );
				}
				if( relatedLemmaSet.size() > 0 )
				{
					attr.put( "relatedLemmas", relatedLemmaSet );
				}
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
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, true );
		
		String inputPath = p.getNTriplesFolder();
		String outputPath = p.getJSONFolder();
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "convert NTs to JSON format" );
		
		job.setNumReduceTasks( numReduceTasks );

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
