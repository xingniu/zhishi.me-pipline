1、StreamingDriver_Baidu.xml && StreamingDriver_Hudong.xml 处理HTML文件，生成RawStructuredData@HDFS
2、SortByPredicate.xml 生成按类型分割的NTriples@HDFS
3、Segment.java 为IM生成词条特征文本的分词结果->HDFS
4、NTriplesToJSON 生成IM的原始输入文件@HDFS
5、MRMatcher.xml 生成匹配原始文件@HDFS
6、NTStorer.java 从HDFS向Poseidon生成分割压缩NTriples，二次处理生成一些labels，输出ontology定义，在线爬互动的分类树@Poseidon