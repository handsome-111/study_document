package stu.wl.twitter.lucene;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import stu.wl.twitter.util.LoadTools;

/**
 * 创建索引
 * 查询索引
 * @author Administrator
 *
 */
public class FirstLucene {
	/**
	 * 创建索引
	 */
	public void createIndex() throws Exception {
		String path = LoadTools.getSystemPath()+"/lucene/index/";
		//创建索引存放目录
		Directory directory = FSDirectory.open(Paths.get(path));
		//创建分析器
		Analyzer analyzer = new StandardAnalyzer();			//标准，官方分析器
		//指定分析器
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		/**
		 * 参数1：指定索引库的存放位置
		 * 参数2：指定一个分析器
		 */
		IndexWriter indexWriter = new IndexWriter(directory, config);
		//需要存入索引的文件夹
		File file = new File("C:\\Users\\Administrator\\Desktop\\luceneDocument");
		File files[] = file.listFiles();
		for(File f : files){
			//声明文档(相当于数据库的行)
			Document document = new Document();
			/**
			 * 以下是声明字段
			 */
			//文件名
			String fileName = f.getName();
			Field fileNameField = new TextField("fieldName",fileName,Store.YES);
			//文件大小
			int fileSize = (int) f.length();
			Field fileSizeField = new IntPoint("fileSize",fileSize);
			//文件路径
			String filePath = f.getPath();
			Field filePathField = new StoredField("filePath",filePath);
			//文件内容
			String content = FileUtils.readFileToString(f, "GBK");
			Field contentField = new TextField("content",content,Store.YES);
			document.add(fileNameField);
			document.add(fileSizeField);
			document.add(filePathField);
			document.add(contentField);
			
			indexWriter.addDocument(document);
		}
		indexWriter.close();
	}
	
	/**
	 * 查询索引
	 */
	public void queryIndex() throws Exception {
		String path = LoadTools.getSystemPath()+"/lucene/index/";
		//索引库目录
		Directory directory = FSDirectory.open(Paths.get(path));
		//Directory directory = new RAMDirectory();		//内存索引库
		//打开索引库
		IndexReader indexReader = DirectoryReader.open(directory);
		//搜索索引对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//查询对象，并指定要查询的域
		Query query = new TermQuery(new Term("content","关"));
		//返回评分最高的2条记录
		TopDocs topDocs = indexSearcher.search(query, 2);
		//获取所有文档(相当于数据库的结果行)
		ScoreDoc scoreDocs[] = topDocs.scoreDocs;
		for(ScoreDoc scoreDoc : scoreDocs){
			//返回文档id(相当于数据库的主键id)
			int doc = scoreDoc.doc;
			//获取具体的文档（相当于数据库的行）
			Document document = indexSearcher.doc(doc);
			String fileName = document.get("fieldName");
			String content = document.get("content");
			String filePath = document.get("filePath");
			String fileSize = document.get("fileSize");
			System.out.println(fileName+","+content+","+filePath+","+fileSize);
		}
		indexReader.close();
	}
	public static void testTokenStream() throws Exception {
		Analyzer analyzer = new MyIKAnalyzer();
		TokenStream tokenStream = analyzer.tokenStream("test", "飞龙在天再低你是不是傻逼");
		CharTermAttribute cta = tokenStream.addAttribute(CharTermAttribute.class);
		OffsetAttribute oa = tokenStream.addAttribute(OffsetAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()){
			System.out.println(cta);
		}
	}
	public static void main(String[] args) throws Exception {
		System.out.println("xxxx");
		testTokenStream();
	}
}
