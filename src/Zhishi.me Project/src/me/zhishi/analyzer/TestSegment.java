package me.zhishi.analyzer;
import junit.framework.Assert;


import org.junit.Test;

public class TestSegment {

	@Test
	public void testAuthor() {		
		Assert.assertEquals(2, InfoboxAnalyzer.segement("《今天你可以不生气：让你快乐每一天的心理学智慧》","（韩）全谦求 著 千太阳 译").size());
		Assert.assertEquals("全谦求", InfoboxAnalyzer.segement("《今天你可以不生气：让你快乐每一天的心理学智慧》","（韩）全谦求 著 千太阳 译").get(0));
		Assert.assertEquals("千太阳", InfoboxAnalyzer.segement("《今天你可以不生气：让你快乐每一天的心理学智慧》","（韩）全谦求 著 千太阳 译").get(1));
		
		Assert.assertEquals(3, InfoboxAnalyzer.segement("《办公室超人》","（美）艾斯勒（Axclrod，A.） 著 曲涛，杨康博 译").size());
		Assert.assertEquals("艾斯勒", InfoboxAnalyzer.segement("《办公室超人》","（美）艾斯勒（Axclrod，A.） 著 曲涛，杨康博 译").get(0));
		Assert.assertEquals("曲涛", InfoboxAnalyzer.segement("《办公室超人》","（美）艾斯勒（Axclrod，A.） 著 曲涛，杨康博 译").get(1));
		Assert.assertEquals("杨康博", InfoboxAnalyzer.segement("《办公室超人》","（美）艾斯勒（Axclrod，A.） 著 曲涛，杨康博 译").get(2));
		
		Assert.assertEquals(1, InfoboxAnalyzer.segement("《〈颅囱经〉解密及临床应用》","百解比丘　译著").size());
		Assert.assertEquals("百解比丘", InfoboxAnalyzer.segement("《〈颅囱经〉解密及临床应用》","百解比丘　译著").get(0));
		
		Assert.assertEquals(3, InfoboxAnalyzer.segement("《公司理财原理》","理查德A·布雷利 / 斯图尔特C·迈尔斯 / 弗兰克林·艾伦").size());
		Assert.assertEquals("理查德A·布雷利", InfoboxAnalyzer.segement("《公司理财原理》","理查德A·布雷利 / 斯图尔特C·迈尔斯 / 弗兰克林·艾伦").get(0));
		Assert.assertEquals("斯图尔特C·迈尔斯", InfoboxAnalyzer.segement("《公司理财原理》","理查德A·布雷利 / 斯图尔特C·迈尔斯 / 弗兰克林·艾伦").get(1));
		Assert.assertEquals("弗兰克林·艾伦", InfoboxAnalyzer.segement("《公司理财原理》","理查德A·布雷利 / 斯图尔特C·迈尔斯 / 弗兰克林·艾伦").get(2));
	
		Assert.assertEquals("《唉，我的沧桑50年（1959至今）》", InfoboxAnalyzer.segement("《唉，我的沧桑50年（1959至今）》","《唉，我的沧桑50年（1959至今）》").get(0));
		
		Assert.assertEquals(9, InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").size());
		Assert.assertEquals("迈克尔·洛克尔", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(0));
		Assert.assertEquals("Michael Rooker", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(1));
		Assert.assertEquals("罗伯特·帕特里克", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(2));
		Assert.assertEquals("Robert Patrick", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(3));
		Assert.assertEquals("Diane DiLascio", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(4));
		Assert.assertEquals("路易丝·曼迪路亚", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(5));
		Assert.assertEquals("Louis Mandylor", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(6));
		Assert.assertEquals("James Kisicki", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(7));
		Assert.assertEquals("Darnell Suttles", InfoboxAnalyzer.segement("《物极必反》","迈克尔·洛克尔，Michael Rooker，罗伯特·帕特里克，Robert Patrick，，Diane DiLascio，路易丝·曼迪路亚，Louis Mandylor，，James Kisicki，，Darnell Suttles").get(8));
		
		Assert.assertEquals("辖王沟圈村", InfoboxAnalyzer.segement("郭原乡","辖王沟圈村、西杨村、寺沟村8个村，85个村民小组").get(0));
		Assert.assertEquals("西杨村", InfoboxAnalyzer.segement("郭原乡","辖王沟圈村、西杨村、寺沟村8个村，85个村民小组").get(1));
		Assert.assertEquals("寺沟村8个村", InfoboxAnalyzer.segement("郭原乡","辖王沟圈村、西杨村、寺沟村8个村，85个村民小组").get(2));
		Assert.assertEquals("85个村民小组", InfoboxAnalyzer.segement("郭原乡","辖王沟圈村、西杨村、寺沟村8个村，85个村民小组").get(3));
	}
	
	@Test
	public void testParen() {
		Assert.assertEquals(1, InfoboxAnalyzer.segement("Ⅱ优838","禾本科(Poaceae；Gramineae)").size());
		Assert.assertEquals("禾本科", InfoboxAnalyzer.segement("Ⅱ优838","禾本科(Poaceae；Gramineae)").get(0));
	}

}

//TODO 空格        类别