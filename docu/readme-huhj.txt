配置Tips
	创建Mysql数据库为utf8，手工建表，数据库url参考sakai;
	 	create DATABASE `contineo` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci 
 		jdbc:mysql://localhost:3306/contineo?useUnicode=true&characterEncoding=UTF-8
 	中文分词	?
	服务器操作系统的语言设置会影响浏览器端的默认语言
发布
	1）手工
		mvn install 
		手动将war包发布到tomcat目录；
	2）自动
		web应用=》contineo-web：  		ant update（默认）
		语言插件=》contineo-lang-pt：	ant deploy
	3）contineo-lang-pt
		顶层pom.xml中没有包含该模块，需手动编译 mvn install，然后ant deploy
	
	说明：mvn clean
	
技术
	spring 2.0.7
	hibernate 
	jsf 替换了Struts
	iceFaces 引入AJAX特性,1.6.2
	lucene 2.3.2
	JPF
	Axis2-1.3 web service 开源项目，外部IT系统集成
	Maven 2

contineo-web/
	src/main/
		assembly/
			assemblyapi.xml				?
		java/
		resources/
			org/contineo/web/i18n/
				application.properties
			context.properties			系统配置（DB/目录/项目资源/skin）,运行时替换context.xml
			context.xml					spring配置，含支持的文件类型定义
			dbms.xml					安装步骤中的数据库配置sample
			log4j.xml					日志配置：logs/dms.log, /logs/dms.log.html
		webapp/
			setup/		有tomcat权限控制
			skins/
			WEB-INF/
				boot.properties
				faces-config.xml	语言设置
分词器
	org.contineo.core.searchengine.crawler.LuceneAnalyzerFactory.java 
		28行SnowballAnalyzer构造函数构造分词器，但实际上没有对应中文的类
		只此一处用到SnowballAnalyzer，可用CJKAnalyzer替换
		建索引和输入查询条件的分词都通过此处
			