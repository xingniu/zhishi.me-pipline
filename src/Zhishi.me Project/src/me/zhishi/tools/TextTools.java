package me.zhishi.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTools
{
	/**
	 * Reverse the given string.
	 * @param str
	 * 			the string that to be reversed
	 * @return
	 * 			reversed string
	 */
	public static String reverse( String str )
	{
		return (new StringBuffer( str )).reverse().toString();
	}
	
	/**
	 * Get Unicode expression of the given string.
	 * For N-Triples encoding (http://www.w3.org/TR/rdf-testcases/#ntrip_strings).
	 * @param str
	 * 			the string that to be encoded
	 * @return
	 * 			encoded string
	 */
	public static String getUnicode( String str )
	{
		String unicode = "";
		for( int i = 0; i < str.length(); ++i )
		{
			char ch = str.charAt( i );
			if( ch > 0xFFF && ch <= 0xFFFF )
			{
				unicode += "\\u" + Integer.toHexString( ch ).toUpperCase();
			}
			else if( ch > 0xFF && ch <= 0xFFF )
			{
				unicode += "\\u0" + Integer.toHexString( ch ).toUpperCase();
			}
			else if( ch >= 0x7F && ch <= 0xFF )
			{
				unicode += "\\u00" + Integer.toHexString( ch ).toUpperCase();
			}
			else if( ch <= 0x7E )
			{
				if( ch <= 0xF )
				{
					if( ch == 0x9 )
						unicode += "\\t";
					else if( ch == 0xA )
						unicode += "\\n";
					else if( ch == 0xD )
						unicode += "\\r";
					else
						unicode += "\\u000" + Integer.toHexString( ch ).toUpperCase();
				}
				else if( ch > 0xF && ch <= 0x1F )
				{
					unicode += "\\u00" + Integer.toHexString( ch ).toUpperCase();
				}
				else
				{
					if( ch == 0x22 )
						unicode += "\\\"";
					else if( ch == 0x5C )
						unicode += "\\\\";
					else
						unicode += ch;
				}
			}
			else
			{
				unicode += "\\U" + Integer.toHexString( ch ).toUpperCase();
			}
		}
		return unicode;
	}
	
	/**
	 * Convert Unicode expression to the original string.
	 * For N-Triples decoding (http://www.w3.org/TR/rdf-testcases/#ntrip_strings).
	 * @param str
	 * 			the string that to be decoded
	 * @return
	 * 			decoded string
	 */
	public static String UnicodeToString( String str )
	{
		Pattern pattern = Pattern.compile( "(\\\\u(\\p{XDigit}{4}))|(\\\\U(\\p{XDigit}{8}))" );
		Matcher matcher = pattern.matcher( str );
		char ch;
		while( matcher.find() )
		{
			ch = (char) Integer.parseInt( matcher.group( 0 ).substring( 2 ), 16 );
			str = str.replace( matcher.group( 0 ), ch + "" );
		}
		str = str.replace( "\\t", "\t" );
		str = str.replace( "\\n", "\n" );
		str = str.replace( "\\r", "\r" );
		str = str.replace( "\\\"", "\"" );
		str = str.replace( "\\\\", "\\" );
		return str;
	}
	
	/**
	 * Encode URL
	 * @param str
	 * 			the URL that to be encoded
	 * @return
	 * 			encoded URL
	 */
	public static String encoder( String str )
	{
		try
		{
			return URLEncoder.encode( str, "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Broken VM does not support UTF-8" );
		}
	}
	
	/**
	 * Decode URL
	 * @param str
	 * 			the URL that to be decoded
	 * @return
	 * 			decoded URL
	 */
	public static String decoder( String str )
	{
		try
		{
			return URLDecoder.decode( str, "UTF-8" );
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException( "Broken VM does not support UTF-8" );
		}
		catch( Exception e )
		{
			return "";
		}
	}
	
	public static double similarity( String str1, String str2 )
	{
		double threshold = 10;
		int length_max = Math.max( str1.length(), str2.length() );
		int length_min = Math.min( str1.length(), str2.length() );
		length_min = (int) Math.min( length_min, threshold );
		return 1-LevenshteinDistance( str1, str2 )*(2-length_min/threshold)/length_max;
	}
	
	public static int LevenshteinDistance( String str1, String str2 )
	{
		int m = str1.length();
		int n = str2.length();
		int[][] matrix = new int[m+1][n+1];
		              
		for( int i = 0; i <= m; ++i )
			matrix[i][0] = i;
		for( int j = 0; j <= n; ++j )
			matrix[0][j] = j;

		for( int j = 1; j <= n; ++j )
		{
			for( int i = 1; i <= m; ++i )
			{
				if( str1.charAt( i-1 ) == str2.charAt( j-1 ) )
				{
					matrix[i][j] = matrix[i-1][j-1];
				}
				else
				{
					matrix[i][j] = Math.min( matrix[i-1][j] + 1, matrix[i][j-1] + 1 );
					matrix[i][j] = Math.min( matrix[i-1][j-1] + 1, matrix[i][j] );
				}
			}
		}
		return matrix[m][n];
	}
}
