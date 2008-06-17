配置Tips
	创建Mysql数据库为utf8，手工建表，数据库url参考sakai;
	 	create DATABASE `contineo` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci 
 		jdbc:mysql://localhost:3306/edlt?useUnicode=true&characterEncoding=UTF-8
 	中文分词
 	
技术
	spring 2.0.7
	hibernate 
	jsf 替换了Struts
	iceFaces 引入AJAX特性
	lucene 2.3.2
	JPF
	Axis2-1.3 外部IT系统集成
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
			