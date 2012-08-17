1、将打包的百科网页上传至HDFS的/Users/xingniu/CLOD/BaikePages/文件夹下
2、执行StreamingFileList.java：生成Streaming程序用的FileList
   @HDFS#/Users/xingniu/CLOD/BaikePages/*/
3、Ant执行StreamingDriver_Baidu.xml和StreamingDriver_Hudong.xml：处理HTML文件，生成RawStructuredData
   @HDFS#/Users/xingniu/CLOD/RawStructuredData/
4、Ant执行SortByPredicate.xml：为百度和互动生成按谓词类型分割的NTriples
   @HDFS#/Users/xingniu/CLOD/NTriples/


3、Segment.java 为IM生成词条特征文本的分词结果->HDFS
4、NTriplesToJSON 生成IM的原始输入文件@HDFS
5、MRMatcher.xml 生成匹配原始文件@HDFS
6、LookupIndexer.xml 生成lookup索引@HDFS
7、NTStorer.java 从HDFS向Poseidon生成分割压缩NTriples，二次处理生成一些labels，输出ontology定义，在线爬互动的分类树@Poseidon