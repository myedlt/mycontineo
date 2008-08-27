编译样例：ant

war文件位置dist\samples, 发布到Tomcat即可

集成ICEfaces
	第一步、改文件为jspx。 
		改扩展名：XML-compliant JSP Page
		移除JSP taglib指令，改用JSF<f:view>元素声明

	第二步、在web.xml注册ICEfaces servlets
		作为JSF的扩展，有自己的FacesServlet，还有BlockingServlet用于控制一部更新
		iface映射
		=》JSF组件该有ICEfaces D2D RenderKit渲染
	第三步
	第四步
	第五步、应用样式
	第六步、使用Facelets

ICEfaces官网上有Eclipse 插件可用
