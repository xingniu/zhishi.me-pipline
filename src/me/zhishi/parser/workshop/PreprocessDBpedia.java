package me.zhishi.parser.workshop;

import java.io.IOException;
import java.util.HashSet;

import me.zhishi.tools.SmallTools;
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
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class PreprocessDBpedia
{
	private static int numReduceTasks = 20;
	
	private static HashSet<String> contents = new HashSet<String>();
	static
	{
		contents.add( "abstract" );
		contents.add( "category" );
		contents.add( "articleLink" );
		contents.add( "dbpediaLink" );
		contents.add( "externalLink" );
		contents.add( "infobox" );
		contents.add( "internalLink" );
		contents.add( "label" );
		contents.add( "propertyDefinition" );
		contents.add( "redirect" );
		contents.add( "skosCat" );
		
//		contents.add( "disambiguation" );
//		contents.add( "image" );
//		contents.add( "imageInfo" );
	}
	
	public static class SortBySubject extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			if( value.toString().startsWith( "<" ) )
			{
				TripleReader tr = new TripleReader( value.toString() );
				if( tr.getBareSubject().startsWith( URICenter.namespace_zhdbpedia ) )
				{
					URICenter uc = new URICenter( URICenter.source_name_zhwiki );
					
					String object = tr.getObject();
					
					String subject = tr.getBareSubject();
					if( subject.startsWith( URICenter.namespace_zhdbpedia_resource ) )
					{
						String sub = tr.getIRISubjectContent();
						if( sub.startsWith( "Category:" ) )
							subject = uc.getCategoryURI( sub.replaceFirst( "Category:", "" ) );
						else
							subject = uc.getResourceURI( sub );
					}
					else if( subject.startsWith( URICenter.namespace_zhdbpedia_property ) )
						subject = uc.getPropertyPredicate( tr.getIRISubjectContent() );
					
					String predicate = tr.getBarePredicate();
					if( predicate.equals( URICenter.predicate_dbpedia_abstract ) )
						predicate = URICenter.predicate_abstract;
					else if( predicate.equals( URICenter.predicate_dc_subject ) )
					{
						predicate = URICenter.predicate_category;
						object = "<" + uc.getCategoryURI( tr.getIRIObjectContent().replaceFirst( "Category:", "" ) ) + ">";
					}
					else if( predicate.equals( URICenter.predicate_dbpedia_wikiPageExternalLink ) )
						predicate = URICenter.predicate_externalLink;
					else if( predicate.startsWith( URICenter.namespace_zhdbpedia_property ) )
					{
						String property = tr.getIRIPredicateContent();
						if( property.equals( "wikiPageUsesTemplate" ) )
							return;
						predicate = uc.getPropertyPredicate( property );
						if( object.startsWith( "<" + URICenter.namespace_zhdbpedia_resource ) )
							object = "<" + uc.getResourceURI( tr.getIRIObjectContent() ) + ">";
					}
					else if( predicate.equals( URICenter.property_geo_lat ) )
						predicate = uc.getPropertyPredicate( "纬度" );
					else if( predicate.equals( URICenter.property_geo_long ) )
						predicate = uc.getPropertyPredicate( "经度" );
					else if( predicate.equals( URICenter.predicate_dbpedia_wikiPageWikiLink ) )
					{
						predicate = URICenter.predicate_internalLink;
						object = "<" + uc.getResourceURI( tr.getIRIObjectContent() ) + ">";
					}
					else if( predicate.equals( URICenter.predicate_dbpedia_wikiPageRedirects ) )
					{
						predicate = URICenter.predicate_redirect;
						object = "<" + uc.getResourceURI( tr.getIRIObjectContent() ) + ">";
					}
					else if( predicate.equals( URICenter.predicate_skos_broader ) )
					{
						object = "<" + uc.getCategoryURI( tr.getIRIObjectContent().replaceFirst( "Category:", "" ) ) + ">";
					}
					
					String triple = TripleWriter.getTripleLine( "<"+subject+">", "<"+predicate+">", object );
					context.write( new Text( subject ), new Text( triple ) );
				}
			}
		}
	}
	
	public static class SortByPredicate extends Reducer<Object, Text, NullWritable, Text>
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
				String pre = tr.getBarePredicate();
				
				if( contents.contains( "abstract" ) && pre.equals( URICenter.predicate_abstract ) )
					mos.write( "abstract", NullWritable.get(), val );
				else if( contents.contains( "category" ) && pre.equals( URICenter.predicate_category ) )
					mos.write( "category", NullWritable.get(), val );
				else if( contents.contains( "articleLink" ) && pre.equals( URICenter.predicate_foaf_isPrimaryTopicOf ) )
				{
					String resource = tr.getBareSubject();
					String articleLink = tr.getBareObject();
					Text pt = new Text( TripleWriter.getResourceObjectTriple( articleLink, URICenter.predicate_foaf_primaryTopic, resource ) );
					Text lang = new Text( TripleWriter.getStringValueTripleAT( articleLink, URICenter.predicate_dc_language, "zh", "en" ) );
					mos.write( "articleLink", NullWritable.get(), pt );
					mos.write( "articleLink", NullWritable.get(), lang );
					mos.write( "articleLink", NullWritable.get(), val );
				}
				else if( contents.contains( "dbpediaLink" ) && pre.equals( URICenter.predicate_owl_sameAs ) )
				{
					if( tr.getBareObject().startsWith( URICenter.namespace_dbpedia ) )
						mos.write( "dbpediaLink", NullWritable.get(), val );
				}
				else if( contents.contains( "externalLink" ) && pre.equals( URICenter.predicate_externalLink ) )
					mos.write( "externalLink", NullWritable.get(), val );
				else if( contents.contains( "infobox" ) && pre.matches( URICenter.domainName + ".*/property/.*" ) )
					mos.write( "infobox", NullWritable.get(), val );
				else if( contents.contains( "internalLink" ) && pre.equals( URICenter.predicate_internalLink ) )
					mos.write( "internalLink", NullWritable.get(), val );
				else if( contents.contains( "label" ) && pre.equals( URICenter.predicate_rdfs_label ) && tr.getBareSubject().startsWith( URICenter.namespace_zhwiki + "resource" ) )
					mos.write( "label", NullWritable.get(), val );
				else if( contents.contains( "propertyDefinition" ) && pre.equals( URICenter.predicate_rdfs_label ) && tr.getBareSubject().startsWith( URICenter.namespace_zhwiki + "property" ) )
					mos.write( "propertyDefinition", NullWritable.get(), val );
				else if( contents.contains( "redirect" ) && pre.equals( URICenter.predicate_redirect ) )
					mos.write( "redirect", NullWritable.get(), val );
				else if( contents.contains( "skosCat" ) && pre.equals( URICenter.predicate_skos_prefLabel ) )
				{
					Text type = new Text( TripleWriter.getResourceObjectTriple( tr.getBareSubject(), URICenter.predicate_rdf_type, URICenter.class_skos_concept ) );
					mos.write( "skosCat", NullWritable.get(), type );
					mos.write( "skosCat", NullWritable.get(), val );
				}
				else if( contents.contains( "skosCat" ) && pre.equals( URICenter.predicate_skos_broader ) )
				{
					Text narrower = new Text( TripleWriter.getResourceObjectTriple( tr.getBareObject(), URICenter.predicate_skos_narrower, tr.getBareSubject() ) );
					mos.write( "skosCat", NullWritable.get(), val );
					mos.write( "skosCat", NullWritable.get(), narrower );
				}
				
//				else if( contents.contains( "relatedPage" ) && pre.equals( URICenter.predicate_relatedPage ) )
//					mos.write( "relatedPage", NullWritable.get(), val );
//				else if( contents.contains( "disambiguation" ) && pre.equals( URICenter.predicate_disambiguation ) )
//					mos.write( "disambiguation", NullWritable.get(), val );
//				else if( contents.contains( "image" ) && pre.equals( URICenter.predicate_foaf_depiction ) )
//					mos.write( "image", NullWritable.get(), val );
//				else if( contents.contains( "image" ) && pre.equals( URICenter.predicate_depictionThumbnail ) )
//					mos.write( "image", NullWritable.get(), val );
//				else if( contents.contains( "image" ) && pre.equals( URICenter.predicate_relatedImage ) )
//					mos.write( "image", NullWritable.get(), val );
//				else if( contents.contains( "imageInfo" ) && pre.equals( URICenter.predicate_rdfs_label ) && !tr.getSubject().startsWith( "<" + URICenter.domainName ) )
//					mos.write( "imageInfo", NullWritable.get(), val );
//				else if( contents.contains( "imageInfo" ) && pre.equals( URICenter.predicate_dc_rights ) )
//					mos.write( "imageInfo", NullWritable.get(), val );
//				else if( contents.contains( "imageInfo" ) && pre.equals( URICenter.predicate_foaf_thumbnail ) )
//					mos.write( "imageInfo", NullWritable.get(), val );
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
	
	public static void run( double releaseVersion ) throws Exception
	{
		String source = URICenter.source_name_zhwiki;
		me.zhishi.tools.Path p = new me.zhishi.tools.Path( releaseVersion, source, true );
		
		String inputPath = p.getRawStructuredDataFolder();
		String outputPath = p.getNTriplesFolder() + source + "/";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", me.zhishi.tools.Path.hdfs_fsName );
		FileSystem fs = FileSystem.get( conf );
		fs.delete( new Path( outputPath ), true );
		
		Job job = new Job( conf, "ZHISHI.ME# Localizing DBpedia" );
		
		job.setNumReduceTasks( numReduceTasks );

		job.setJarByClass( PreprocessDBpedia.class );
		job.setMapperClass( SortBySubject.class );
		job.setReducerClass( SortByPredicate.class );
		
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
				System.out.println( "Start moveMerging files: " + s );
				SmallTools.moveMergeFiles( fs, s, p.getNTriplesFile( s ), conf, outputPath, numReduceTasks );
			}
			fs.delete( new Path( outputPath ), true );
		}
	}
}

