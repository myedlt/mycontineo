/*
 * Result.java
 * 
 * Created on 6. November 2003, 16:21
 */
package org.contineo.core.searchengine.search;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.contineo.core.i18n.DateBean;

/**
 * @author Michael Scholz, Marco Meschieri, Alessandro Gasparini
 */
public class Result implements Serializable, ResultInterface {
	private static final long serialVersionUID = 1L;

	private Integer docId = new Integer(0);

	private String name = "";

	private String summary = "";

	private String path = "";

	private int menuId = -1;

	private String type = "";

	private String icon = "";

	private int size = 0;

	private Date date = new Date();

	private Date sourceDate = null;

	private Integer length = new Integer(0);

	private Integer green = new Integer(0);

	private Integer red = new Integer(0);

	public Result() {
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getDocId()
	 */
	public Integer getDocId() {
		return docId;
	}

	public void setDocid(Integer docId) {
		this.docId = docId;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getSummary()
	 */
	public String getSummary() {
		return summary;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getPath()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getMenuId()
	 */
	public int getMenuId() {
		return menuId;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getIcon()
	 */
	public String getIcon() {
		return icon;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getSize()
	 */
	public int getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getGreen()
	 */
	public Integer getGreen() {
		return green;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getRed()
	 */
	public Integer getRed() {
		return red;
	}

	public void setName(String nme) {
		name = nme;
	}

	public void setSummary(String summ) {
		summary = summ;
	}

	public void setPath(String pth) {
		path = pth;
	}

	public void setMenuId(int id) {
		menuId = id;
	}

	public void setType(String typ) {
		type = typ;
		icon = "";

		if (type.equals("PDF")) {
			icon = "pdf.gif";
		} else if (type.equals("DOC") || type.equals("DOT") || type.equals("RTF") || type.equals("SXW")
				|| type.equals("TXT") || type.equals("WPD") || type.equals("KWD") || type.equals("ABW")
				|| type.equals("ZABW") || type.equals("ODT")) {
			icon = "textdoc.gif";
		} else if (type.equals("XLS") || type.equals("XLT") || type.equals("SXC") || type.equals("DBF")
				|| type.equals("KSP") || type.equals("ODS") || type.equals("ODB")) {
			icon = "tabledoc.gif";
		} else if (type.equals("PPT") || type.equals("PPS") || type.equals("POT") || type.equals("SXI")
				|| type.equals("KPR") || type.equals("ODP")) {
			icon = "presentdoc.gif";
		} else if (type.equals("APF") || type.equals("BMP") || type.equals("JPEG") || type.equals("DIB")
				|| type.equals("GIF") || type.equals("JPG") || type.equals("PSD") || type.equals("TIF")
				|| type.equals("TIFF")) {
			icon = "picture.gif";
		} else if (type.equals("HTM") || type.equals("HTML") || type.equals("XML")) {
			icon = "internet.gif";
		} else {
			icon = "document.gif";
		}
	}

	public void setSize(int sze) {
		size = sze;
	}

	public void createScore(float score) {
		float temp = score * 100;
		int tgreen = Math.round(temp);

		if (tgreen < 1) {
			tgreen = 1;
		}

		green = new Integer(tgreen);
		temp = 100 - (score * 100);

		int tred = Math.round(temp);

		if (tred > 99) {
			tred = 99;
		}

		red = new Integer(tred);
	}

	public void setGreen(Integer grn) {
		green = grn;
	}

	public void setRed(Integer rd) {
		red = rd;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#isRelevant(org.contineo.core.searchengine.search.SearchOptions, java.lang.String)
	 */
	public boolean isRelevant(SearchOptions opt, String sourceDate) {
		boolean result = true;

		if ((opt.getFormat() != null) && !opt.getFormat().equals("all")) {
			if (!type.toLowerCase().equals(opt.getFormat())) {
				result = false;
			}
		}

		if (opt.getLengthMin() != null && size < opt.getLengthMin().intValue())
			result = false;

		if (opt.getLengthMax() != null && size > opt.getLengthMax().intValue())
			result = false;

		if (opt.getCreationDateFrom() != null) {
			long diff = opt.getCreationDateFrom().getTime() - date.getTime();
			if (diff > 0)
				result = false;
		}

		if (opt.getCreationDateTo() != null) {
			long diff = opt.getCreationDateTo().getTime() - date.getTime();
			if (diff < 0)
				result = false;
		}

		Date srcDate = null;
		if (StringUtils.isNotEmpty(sourceDate))
			srcDate = DateBean.dateFromCompactString(sourceDate);

		if (opt.getSourceDateFrom() != null && srcDate != null) {
			if (srcDate.before(opt.getSourceDateFrom()))
				result = false;
		}

		if (opt.getSourceDateTo() != null && srcDate != null) {
			if (srcDate.after(opt.getSourceDateTo()))
				result = false;
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getLengthCategory()
	 */
	public int getLengthCategory() {
		int len = length.intValue();

		if (len > 60000) {
			return 5;
		}

		if (len > 18000) {
			return 4;
		}

		if (len > 3000) {
			return 3;
		}

		if (len > 600) {
			return 2;
		}

		return 1;
	}

	public void setDate(Date d) {
		date = d;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getDateCategory()
	 */
	public int getDateCategory() {
		long diff = new Date().getTime() - date.getTime();
		long days = diff / 1000 / 60 / 60 / 24; // 1000-sec , 60-min , 60-h ,
		// 24-day

		if (days < 8) {
			return 0;
		}

		if (days < 29) {
			return 1;
		}

		if (days < 92) {
			return 2;
		}

		if (days < 366) {
			return 3;
		}

		return 4;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getSourceDate()
	 */
	public Date getSourceDate() {
		return sourceDate;
	}

	public void setSourceDate(Date sourceDate) {
		this.sourceDate = sourceDate;
	}

	/* (non-Javadoc)
	 * @see org.contineo.core.searchengine.search.ResultInterface#getDocType()
	 */
	public int getDocType() {
		if (type.equals("PDF") || type.equals("DOC") || type.equals("TXT") || type.equals("RTF") || type.equals("HTML")
				|| type.equals("HTM") || type.equals("SXW") || type.equals("WPD") || type.equals("PS")
				|| type.equals("KWD")) {
			return 0;
		}

		if (type.equals("XLS") || type.equals("SXC") || type.equals("KSP")) {
			return 1;
		}

		if (type.equals("PPT") || type.equals("PPS") || type.equals("SXI") || type.equals("KPR")) {
			return 2;
		}

		return 3;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
