package me.zhishi.lab;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

import me.zhishi.tools.TextTools;
import me.zhishi.tools.TripleReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CommonSuffix
{
	private static Pattern noSenseSuffix = Pattern.compile( "[》>）)？?\\d\\w]" );
	
	public static class CommonSuffixMapper extends Mapper<Object, Text, Text, Text>
	{
		public void map( Object key, Text value, Context context ) throws IOException, InterruptedException
		{
			String triple = value.toString();
			TripleReader tr = new TripleReader( triple );
			String label = tr.getBareObject();
			label = label.replaceAll( " \\(.*?\\)$", "" );
			label = label.replaceAll( "\\[.*?\\]$", "" );
			//2: At least 2 chars
			for( int i = label.length() - 2; i >= 0; --i )
			{
				String suffix = label.substring( i, label.length() );
				if( noSenseSuffix.matcher( suffix ).find() )
					return;
				context.write( new Text(TextTools.reverse( suffix )), new Text(label) );
			}
		}
	}

	public static class CommonSuffixReducer extends Reducer<Text, Text, Text, Text>
	{
		public void reduce( Text key, Iterable<Text> values, Context context ) throws IOException, InterruptedException
		{
			HashSet<String> labelSet = new HashSet<String>();
			
			String suffix = TextTools.reverse( key.toString() );
			boolean suffixEQlabel = false;

			for( Text val : values )
			{
				String label = val.toString();
				if( label.equals( suffix ) )
					suffixEQlabel = true;
				if( !labelSet.contains( label ) )
					labelSet.add( label );
			}
			
			context.setStatus( "Size: " + labelSet.size() );
			
			if( suffixEQlabel && labelSet.size() >=10 )
			{
				int counter = 0;
				String out = "[";
				for( String label : labelSet )
				{
					if( counter > 1000 )
						break;
					out += label + " ";
					counter++;
				}
				out += "]";
				context.write( new Text(suffix), new Text( out ) );
			}
		}
	}

	public static void main( String[] args ) throws Exception
	{
		String inputPath = "/Users/xingniu/CLOD/Lab/LabelsNTriples";
		String outputPath = "/Users/xingniu/CLOD/Lab/CommonSuffix";
		
		Configuration conf = new Configuration();
		
		conf.set( "fs.default.name", "hdfs://172.16.7.14" );
		FileSystem fs = FileSystem.get( conf ); 
		fs.delete( new Path( outputPath ), true );
		fs.delete( new Path( outputPath+".txt" ), true );
		
		Job job = new Job( conf, "Find Common Suffix" );
		
		job.setNumReduceTasks( 20 );

		job.setJarByClass( CommonSuffix.class );
		job.setMapperClass( CommonSuffixMapper.class );
		job.setReducerClass( CommonSuffixReducer.class );

		job.setOutputKeyClass( Text.class );
		job.setOutputValueClass( Text.class );

		FileInputFormat.addInputPath( job, new Path( inputPath ) );
		FileOutputFormat.setOutputPath( job, new Path( outputPath ) );

		if( job.waitForCompletion( true ) )
		{
			FileUtil.copyMerge( fs, new Path(outputPath), fs, new Path(outputPath+".txt"), false, conf, "" );
		}
	}
}
