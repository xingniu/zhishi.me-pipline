package me.zhishi.parser.driver;

import java.util.HashSet;

public class TypeNormalize {
	public static HashSet<String> List = new HashSet<String>();
	
	public static HashSet<String> List() {
		//time
		List.add("秒");		List.add("分");		List.add("时");		List.add("天");
		
		//weight
		List.add("克");		List.add("公斤");		List.add("吨");		List.add("磅");		
		List.add("斤");
		
		//length
		List.add("米");		List.add("公里");		List.add("毫米");		List.add("厘米");
		
		//area
		List.add("平方公里");	List.add("平方米");	List.add("亩");		List.add("公顷");
		
		//volume
		List.add("立方米");	List.add("立方厘米");	List.add("升");		List.add("毫升");
		
		//speed
		List.add("公里/小时");	List.add("米/小时");	
		
		//size
		List.add("MB");		List.add("GB");		List.add("G");
		
		//other
		List.add("kw");		List.add("/平方公里");	List.add("/平方米");	List.add("A");
		List.add("摄氏度");	List.add("KW/rpm");	List.add("N·m/rpm");List.add("");
		
		List.add("");		List.add("");		List.add("");		List.add("");
		return List;
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
		oc = oc.replaceAll("^h$", "时");
		oc = oc.replaceAll("^g$", "克");
		oc = oc.replaceAll("^[kK][gG]$", "公斤");
		oc = oc.replaceAll("^lbs$", "磅");
		oc = oc.replaceAll("^[mM]$", "米");
		oc = oc.replaceAll("^[cC][mM]$", "厘米");
		oc = oc.replaceAll("^[kK][mM]$", "公里");
		oc = oc.replaceAll("^[mM][mM]$", "毫米");
		oc = oc.replaceAll("^公分$", "厘米");
		oc = oc.replaceAll("^平米$", "平方米");
		oc = oc.replaceAll("^立方$", "立方米");
		oc = oc.replaceAll("^L$", "升");
		oc = oc.replaceAll("^cc$", "毫升");
		oc = oc.replaceAll("^mL$", "毫升");
		oc = oc.replaceAll("^[kK][mM]/[hH]$", "公里/小时");
		oc = oc.replaceAll("^℃$", "摄氏度");
		
		
		
		return oc;
	}
}
