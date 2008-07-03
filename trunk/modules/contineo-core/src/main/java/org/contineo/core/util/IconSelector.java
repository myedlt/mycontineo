package org.contineo.core.util;

/**
 * utility class to select an icon based on a file extension
 * @author Sebastian Stein
 */
public class IconSelector {
	
	/** returns path to menu icon by parsing the provided file extension */
	public static String selectIcon(String ext) {
		String icon = "";
		ext = ext.toLowerCase();

		if (ext == null || ext.equalsIgnoreCase(""))
			icon = "document.gif";
		else if (ext.equals("pdf"))
			icon = "pdf.gif";
		
		//zml edit 分离出常用的文件格式及对应图片
		else if (ext.equals("doc"))
			icon = "doc.gif";
		else if (ext.equals("txt"))
			icon = "txt.gif";
		else if (ext.equals("ppt"))
			icon = "ppt.gif";
		else if (ext.equals("xls"))
			icon = "xls.gif";
		else if (ext.equals("htm") || ext.equals("html"))
			icon = "internet.gif";
			
		else if (ext.equals("docx") || ext.equals("dot") || ext.equals("rtf") || ext.equals("sxw") || ext.equals("xml")
				|| ext.equals("wpd") || ext.equals("kwd") || ext.equals("abw") || ext.equals("zabw") || ext.equals("odt"))
			icon = "textdoc.gif";
		else if (ext.equals("xslx") || ext.equals("xlt") || ext.equals("sxc") || ext.equals("dbf") || ext.equals("ksp")
				|| ext.equals("ods") || ext.equals("odb"))
			icon = "tabledoc.gif";
		else if (ext.equals("pptx") || ext.equals("pps") || ext.equals("pot") || ext.equals("sxi") || ext.equals("kpr")
				|| ext.equals("odp"))
			icon = "presentdoc.gif";
		else if (ext.equals("apf") || ext.equals("bmp") || ext.equals("cur") || ext.equals("dib") || ext.equals("gif")
				|| ext.equals("jpg") || ext.equals("psd") || ext.equals("tif") || ext.equals("tiff"))
			icon = "picture.gif";

        else if (ext.equals("mail"))
            icon = "mail.gif";
        else
			icon = "document.gif";
		
		return icon;
	}
}