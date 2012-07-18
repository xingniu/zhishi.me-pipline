package me.zhishi.parser.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeNormalize {
	public static HashSet<String> List = new HashSet<String>();
	public static HashMap<String, Double> Table = new HashMap<String , Double>();
	
	static
	{
		List();	
		Table();
	}
	
	public static String CheckType(String oc)
	{
		oc = RemoveParen(oc);
		oc = oc.replaceAll( "[￥]", "" );
		
		Pattern TypePatt = Pattern.compile("[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
		Pattern LeadZero = Pattern.compile("0[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
		
		if ( TypePatt.matcher( oc ).matches() && !LeadZero.matcher( oc ).matches() )
		{
			String valuetype = null;
			if ( oc.contains(".") ) valuetype = "1";
			else valuetype = "0";
			oc = oc.replaceFirst("[0-9]+(\\.[0-9]+)?", "");
			oc = oc.replaceAll("[起余多　左右以上下 .]", "");
			oc = oc.replaceAll("[十百千万兆亿]", "");
			oc = TypeNormalize.Normalize(oc);
			if ( List.contains(oc) ) return valuetype + oc;
			else return null;
		}
		
		return null;
	}
	
	public static String getBareType(String oc)
	{
		oc = RemoveParen(oc);
		oc = oc.replaceAll( "[￥]", "" );
		
		Pattern TypePatt = Pattern.compile("[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
		Pattern LeadZero = Pattern.compile("0[0-9]+(\\.[0-9]+)?[^0-9，、。；,;]*$");
		
		if ( TypePatt.matcher( oc ).matches() && !LeadZero.matcher( oc ).matches() )
		{
			oc = oc.replaceFirst("[0-9]+(\\.[0-9]+)?", "");
			oc = oc.replaceAll("[起余多　左右以上下 .]", "");
			oc = oc.replaceAll("[十百千万兆亿]", "");
			oc = TypeNormalize.Normalize(oc);
			return oc;
		}
		
		return null;
	}
	
	public static String getValue(String oc)
	{
		oc = RemoveParen(oc);
		oc = oc.replaceAll( "[￥]", "" );
		
		Pattern ValuePatt = Pattern.compile("[0-9]+(\\.[0-9]+)?");
		Matcher m = ValuePatt.matcher(oc);
		
		m.find();
		String ret = m.group();
		
		int dot = 0;
		int tmp = ret.indexOf("."); 
		if (  tmp > -1 ) dot = tmp - ret.length() + 1;  
		
		while ( oc.indexOf("十") > -1 )	{	++dot;	oc = oc.replaceFirst("十", "");	}
		while ( oc.indexOf("百") > -1 )	{	dot += 2;	oc = oc.replaceFirst("百", "");	}
		while ( oc.indexOf("千") > -1 )	{	dot += 3;	oc = oc.replaceFirst("千", "");	}
		while ( oc.indexOf("万") > -1 )	{	dot += 4;	oc = oc.replaceFirst("万", "");	}
		while ( oc.indexOf("兆") > -1 )	{	dot += 6;	oc = oc.replaceFirst("兆", "");	}
		while ( oc.indexOf("亿") > -1 )	{	dot += 8;	oc = oc.replaceFirst("亿", "");	}
		
		ret = ret.replaceAll("\\.", "");
		if ( dot < 0 )
		{
			ret = ret.substring(0, ret.length() + dot) + "." + ret.substring(ret.length() + dot, ret.length());
		}
		else
		{
			for ( int i = 0; i < dot; ++i ) ret = ret + "0";
			while ( ret.indexOf("0") == 0 ) ret = ret.replaceFirst("0", "");
		}
		return ret;
	}
	
	public static boolean isDate(String oc)
	{
		Pattern DatePatt = Pattern.compile("^[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]$");
		return DatePatt.matcher( oc ).matches();
	}
	
	public static void List() {
		//time
		List.add("秒");		List.add("分");		List.add("时");		List.add("天");
		List.add("月");
		//weight
		List.add("毫克");		List.add("克");		List.add("斤");		List.add("公斤");
		List.add("吨");		List.add("磅");				
		//length
		List.add("毫米");		List.add("厘米");		List.add("米");		List.add("公里");	
		//area
		List.add("平方米");	List.add("亩");		List.add("公顷");		List.add("平方公里");		
		//volume
		List.add("立方厘米");	List.add("升");		List.add("立方米");			
		//speed
		List.add("米/小时");	List.add("公里/小时");	List.add("公里/秒");		
		//size
		List.add("MB");		List.add("GB");		
		//density
		List.add("克/立方厘米");					List.add("克/升");		
		//other
		List.add("kw");		List.add("/平方公里");	List.add("/平方米");	List.add("A");
		List.add("摄氏度");	List.add("KW/rpm");	List.add("N·m/rpm");List.add("升/公里");		
		//null
		List.add("");
	}
	
	public static void Table()
	{
		//time
		Table.put("秒 分", (double) (1/60));				Table.put("分 秒", (double) 60);
		Table.put("秒 时", (double) (1/3600));				Table.put("时 秒", (double) 3600);
		Table.put("秒 天", (double) (1/86400));			Table.put("天 秒", (double) 86400);
		Table.put("分 时", (double) (1/60));				Table.put("时 分", (double) 60);
		Table.put("分 天", (double) (1/1440));				Table.put("天 分", (double) 1440);
		Table.put("时 天", (double) (1/24));				Table.put("天 时", (double) 24);
		//weight
		Table.put("毫克 克", (double) (1/1000));			Table.put("克 毫克", (double) 1000);
		Table.put("毫克 斤", (double) (1/500000));			Table.put("斤 毫克", (double) 500000);
		Table.put("毫克 公斤", (double) (1/1000000));		Table.put("公斤 毫克", (double) 1000000);
		Table.put("毫克 吨", (double) (1/1000000000));		Table.put("吨 毫克", (double) 1000000000);
		Table.put("毫克 磅", (double) 0.00000220462262);	Table.put("磅 毫克", (double) 453592.37);
		Table.put("克 斤", (double) (1/500));				Table.put("斤 克", (double) 500);
		Table.put("克 公斤", (double) (1/1000));			Table.put("公斤 克", (double) 1000);
		Table.put("克 吨", (double) (1/1000000));			Table.put("吨 克", (double) 1000000);
		Table.put("克 磅", (double) 0.00220462262);		Table.put("磅 克", (double) 453.59237);
		Table.put("斤 公斤", (double) (1/2));				Table.put("公斤 斤", (double) 2);
		Table.put("斤 吨", (double) (1/2000));				Table.put("吨 斤", (double) 2000);
		Table.put("斤 磅", (double) 1.10231131);			Table.put("磅 斤", (double) 0.90718474);
		Table.put("公斤 吨", (double) (1/1000));			Table.put("吨 公斤", (double) 1000);
		Table.put("公斤 磅", (double) 2.20462262);			Table.put("磅 公斤", (double) 0.45359237);
		Table.put("吨 磅", (double) 2204.62262);			Table.put("磅 吨", (double) 0.00045359237);
		//length
		Table.put("毫米 厘米", (double) (1/10));			Table.put("厘米 毫米", (double) 10);
		Table.put("毫米 米", (double) (1/1000));			Table.put("米 毫米", (double) 1000);
		Table.put("毫米 公里", (double) (1/1000000));		Table.put("公里 毫米", (double) 1000000);
		Table.put("厘米 米", (double) (1/100));			Table.put("米 厘米", (double) 100);
		Table.put("厘米 公里", (double) (1/100000));		Table.put("公里 厘米", (double) 100000);
		Table.put("米 公里", (double) (1/1000));			Table.put("公里 米", (double) 1000);
		//area
		Table.put("平方米 亩", (double) 0.0015);			Table.put("亩 平方米", (double) (1/0.0015));
		Table.put("平方米 公顷", (double) 0.0001);			Table.put("公顷 平方米", (double) 10000);
		Table.put("平方米 平方公里", (double) 0.000001);		Table.put("平方公里 平方米", (double) 1000000);
		Table.put("亩 公顷", (double) (1/15));				Table.put("公顷 亩", (double) 15);
		Table.put("亩 平方公里", (double) (1/1500));			Table.put("平方公里 亩", (double) 1500);
		Table.put("公顷 平方公里", (double) (1/100));		Table.put("平方公里 公顷", (double) 100);
		//volume
		Table.put("立方厘米 升", (double) (1/1000));			Table.put("升 立方厘米", (double) 1000);
		Table.put("立方厘米 立方米", (double) (1/1000000));	Table.put("立方米 立方厘米", (double) 1000000);
		Table.put("升 立方米", (double) (1/1000));			Table.put("立方米 升", (double) 1000);
		//speed
		Table.put("米/小时 公里/小时", (double) (1/1000));	Table.put("公里/小时 米/小时", (double) 1000);
		Table.put("米/小时 公里/秒", (double) (1/3600000));	Table.put("公里/秒 米/小时", (double) 3600000);
		Table.put("公里/小时 公里/秒", (double) (1/3600));	Table.put("公里/秒 公里/小时", (double) 3600);
		//size
		Table.put("MB GB", (double) (1/1000));			Table.put("GB MB", (double) 1000);
		Table.put("米 GB", (double) (1/1000));			Table.put("GB 米", (double) 1000);
		Table.put("米 MB", (double) 1);					Table.put("MB 米", (double) 1);
		//density
		Table.put("克/立方厘米 克/升", (double) 1000);		Table.put("克/升 克/立方厘米", (double) (1/1000));
		//other
		Table.put("/平方公里 /平方米", (double) (1/1000000));				Table.put("/平方米 /平方公里", (double) 1000000);	
	}
	
	public static String RemoveParen(String oc)
	{
		oc = oc.replaceAll( "\\([^\\(\\)]*\\)", "" );
		oc = oc.replaceAll( "（[^（）]*）", "" );
		oc = oc.replaceAll( "\\([^（）]*）", "" );
		oc = oc.replaceAll( "（[^（）]*\\)", "" );
		oc = oc.replaceAll( "（*", "" );
		oc = oc.replaceAll( "）*", "" );
		oc = oc.replaceAll( "\\(*", "" );
		oc = oc.replaceAll( "\\)*", "" );
		
		return oc;
	}
	
	public static String Normalize(String oc)
	{
		oc = oc.replaceAll("／", "/");
		oc = oc.replaceAll("人名币", "人民币");
		oc = oc.replaceAll("^(元)?人民币$", "元");
		oc = oc.replaceAll("^平方公理$", "平方公里");
		oc = oc.replaceAll("^k㎡$", "平方公里");
		oc = oc.replaceAll("^km²$", "平方公里");
		oc = oc.replaceAll("^平房公里$", "平方公里");
		oc = oc.replaceAll("^[平方]公里$", "平方公里");
		oc = oc.replaceAll("^平方[公里]$", "平方公里");
		oc = oc.replaceAll("^km?$", "平方公里");
		oc = oc.replaceAll("^立方米立方米$", "立方米");
		oc = oc.replaceAll("^立方米每秒立方米$", "立方米");
		oc = oc.replaceAll("^m/s立方米$", "立方米");
		oc = oc.replaceAll("^立方米/秒立方米$", "立方米");
		oc = oc.replaceAll("^㎏$", "公斤");
		oc = oc.replaceAll("^lb$", "lbs");
		oc = oc.replaceAll("^g/ml$", "g/mL");
		oc = oc.replaceAll("^g/l$", "g/L");
		oc = oc.replaceAll("^㎝$", "cm");
		oc = oc.replaceAll("^亩耕地$", "亩");
		oc = oc.replaceAll("^㎡$", "平方米");
		oc = oc.replaceAll("^立方立米$", "立方厘米");
		oc = oc.replaceAll("^[mM]i[nN](s)?$", "min");
		oc = oc.replaceAll("^分种$", "分钟");
		oc = oc.replaceAll("^s$", "秒");
		oc = oc.replaceAll("^分钟$", "分");
		oc = oc.replaceAll("^min$", "分");
		oc = oc.replaceAll("^小时$", "时");
		oc = oc.replaceAll("^[hH]$", "时");
		oc = oc.replaceAll("^g$", "克");
		oc = oc.replaceAll("^[kK][gG]$", "公斤");
		oc = oc.replaceAll("^lbs$", "磅");
		oc = oc.replaceAll("^[mM]$", "米");
		oc = oc.replaceAll("^[cC][mM]$", "厘米");
		oc = oc.replaceAll("^[kK][mM]$", "公里");
		oc = oc.replaceAll("^[mM][mM]$", "毫米");
		oc = oc.replaceAll("^公分$", "厘米");
		oc = oc.replaceAll("^平米$", "平方米");
		oc = oc.replaceAll("^平方$", "平方米");
		oc = oc.replaceAll("^立方$", "立方米");
		oc = oc.replaceAll("^L$", "升");
		oc = oc.replaceAll("^cc$", "毫升");
		oc = oc.replaceAll("^[mM][lL]$", "毫升");
		oc = oc.replaceAll("^毫升$", "立方厘米");
		oc = oc.replaceAll("^[mM][gG]$", "毫克");
		oc = oc.replaceAll("^[kK][mM]/[hH]$", "公里/小时");
		oc = oc.replaceAll("^℃$", "摄氏度");
		oc = oc.replaceAll("^L/公里$", "升/公里");
		oc = oc.replaceAll("^g/cm³$", "克/立方厘米");
		oc = oc.replaceAll("^g/mL$", "克/立方厘米");
		oc = oc.replaceAll("^g/L$", "克/升");
		oc = oc.replaceAll("^KM/S$", "公里/秒");
		oc = oc.replaceAll("^G$", "GB");
		
		return oc;
	}
}
