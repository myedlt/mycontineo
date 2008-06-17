package org.contineo.core.searchengine.search;

import java.util.Date;

public interface ResultInterface {

	public abstract Integer getDocId();

	public abstract String getName();

	public abstract String getSummary();

	public abstract String getPath();

	public abstract int getMenuId();

	public abstract String getType();

	public abstract String getIcon();

	public abstract int getSize();

	public abstract Integer getGreen();

	public abstract Integer getRed();

	public abstract boolean isRelevant(SearchOptions opt, String sourceDate);

	public abstract int getLengthCategory();

	public abstract Date getDate();

	public abstract int getDateCategory();

	public abstract Date getSourceDate();

	public abstract int getDocType();

}