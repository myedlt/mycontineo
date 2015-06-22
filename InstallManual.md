Contineo是一个基于网络的在线文档管理系统，使用这款系统可以帮助用户管理各种流行格式的文档。
Contineo开发过程中就非常注重文档的本身特点和生命周期，并尽可能的方便用户开发、编辑和管理文档的过程。该软件同时也提供了搜索、发布等各种功能以满足用户的需求。它提供了两种文档共享方法，一种是在线直接发布，另外一种是通过电子邮件发送给指定用户。
服务器端依赖：
>=JRE-1.4.0
管理功能包括：
用户管理
组管理
搜索索引管理
备份管理
文档功能包括：
通过HTTP添加文档
下载文档
编辑、删除文档
版本管理
下载控制（权限控制）
目录压缩等
支持的数据库类型包括：
Oracle 8i/9i
IBM DB2 UDB 8.1
MS SQL Server 6.5/7.0/2000
Informix Dynamic Server 9.2/9.3
Sybase ASE 12.5
MySQL 4.1/5.0
PostgreSQL 7.x/8.x
Firebird 1.5.x
SAP DB 7.x
HSQLDB 1.7

首先 需要下载 contineo 的二进制安装包（里面包含个 contineo.war文件），服务器选用 apache tomcat

若jdk用的是1.6 那么 apache最好用 version 6

若jdk用的1.5的那么 apache最好用 version 5

1、 将 contineo.war 文件放到 apache/webapps目录下

2、 修改 Apache Tomacat 目录conf下的文件 tomcat-user.xml

增加一个admin角色：



&lt;role rolename="admin" /&gt;



然后创建一个admin角色的用户：



&lt;user username="admin" password="secret" roles="admin"/&gt;



修改后的 tomcat-user.xml 文件至少包含以下信息：

<?xml version='1.0' encoding='utf-8'?>



&lt;tomcat-users&gt;





&lt;role rolename="manager"/&gt;





&lt;role rolename="admin"/&gt;





&lt;user username="admin" password="secret" roles="manager,admin"/&gt;





&lt;/tomcat-users&gt;



3、在浏览器中输入：http://localhost:8080/contineo/setup （将有如下对话框弹出，输入上面配置中设置的用户名和密码）

clip\_image002

4、 登录后 进行设置（ 第一步先设置工作目录）

clip\_image004

5、 第二步 设置数据库（第一种为contineo自带的一个非常简单的数据管理系统，第二种为其它数据库系统）

clip\_image006

6、第三步 设置数据库（ 在此处 选用 SQL Server2000 ，在此前先利用SQL server创建数据库，名称自定，此处以contineo为例）

clip\_image008

7、创建成功后 如下图所示 contineo 安装成功，并自动创建用户admin 密码也为admin

点击 click here 按钮即进入contineo系统

clip\_image010

8、 进入 contineo系统 http://localhost:8080/contineo/login.iface?rvn=6

用户admin 密码为admin

clip\_image012