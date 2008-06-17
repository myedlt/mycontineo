package org.contineo.core.searchengine.comparision;

import java.io.Serializable;

/**
 * @author Michael Scholz, Marco Meschieri
 */
public class SearchResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name = "";

	private String summary = "";

	private String path = "";

	private int menuId = 0;

	private String icon = "";

	private Integer green = new Integer(0);

	private Integer red = new Integer(0);

	private Double score = new Double(0.0);

	public SearchResult() {
	} // end ctor SearchResult

	public Integer getGreen() {
		return green;
	} // end method getGreen

	public String getIcon() {
		return icon;
	} // end method getIcon

	public int getMenuId() {
		return menuId;
	}

	public String getName() {
		return name;
	} // end method getName

	public String getPath() {
		return path;
	} // end method getPath

	public Integer getRed() {
		return red;
	} // end method getRed

	public Double getScore() {
		return score;
	} // end method getScore

	public String getSummary() {
		return summary;
	} // end method getSummary

	public void setGreen(Integer integer) {
		green = integer;
	} // end method setGreen

	public void setIcon(String string) {
		icon = string;
	} // end method setIcon

	public void setMenuId(int id) {
		menuId = id;
	} // end method setMenuId

	public void setName(String string) {
		name = string;
	} // end method setName

	public void setPath(String string) {
		path = string;
	} // end method setPath

	public void setRed(Integer integer) {
		red = integer;
	} // end method setRed

	/**
	 * @param double1
	 */
	public void setScore(double scr) {
		score = new Double(scr);

		double temp = scr * 100;
		int tgreen = (int) Math.round(temp);

		if (tgreen < 1) {
			tgreen = 1;
		}

		green = new Integer(tgreen);
		temp = 100 - (scr * 100);

		int tred = (int) Math.round(temp);

		if (tred > 99) {
			tred = 99;
		}

		red = new Integer(tred);
	}

	public void setSummary(String string) {
		summary = string;
	}

	@Override
	public boolean equals(Object other) {
		SearchResult sr = (SearchResult) other;
		return getMenuId() == sr.getMenuId();
	}
}