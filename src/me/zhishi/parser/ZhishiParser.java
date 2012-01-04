package me.zhishi.parser;

import java.util.ArrayList;

import me.zhishi.tools.StringPair;

public interface ZhishiParser extends Parser
{
	public String getLabel();
	public String getAbstract();
	public String getRedirect();

	public ArrayList<StringPair> getPictures();
	public ArrayList<StringPair> getProperties();
	
	public ArrayList<String> getCategories();
	public ArrayList<String> getInternalLinks();
	public ArrayList<String> getExternalLinks();
	public ArrayList<String> getRelatedLabels();

	public boolean isDisambiguationPage();

	public static String whitespace = "[\\t\\n\\x0B\\f\\r]";
}
