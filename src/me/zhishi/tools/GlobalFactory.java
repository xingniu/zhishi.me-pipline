package me.zhishi.tools;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import me.zhishi.parser.Parser;
import me.zhishi.parser.driver.ParserDriver;

public class GlobalFactory
{
	private Properties parsers;
	
	public Constructor<? extends Parser> baiduParserConstructor;
	public Constructor<? extends Parser> hudongParserConstructor;
	
	public GlobalFactory()
	{
		try
		{
			parsers = new Properties();
			parsers.load( ParserDriver.class.getResourceAsStream( "/config.ini") );
			baiduParserConstructor = Class
					.forName( (String) parsers.get( URICenter.source_name_baidu ) )
					.asSubclass( Parser.class ).getConstructor( InputStream.class, String.class );
			hudongParserConstructor = Class
					.forName( (String) parsers.get( URICenter.source_name_hudong ) )
					.asSubclass( Parser.class ).getConstructor( InputStream.class, String.class  );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
