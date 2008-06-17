package org.contineo.web.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.i18n.LanguageManager;
import org.contineo.core.searchengine.comparision.Searcher;
import org.contineo.core.searchengine.search.Result;
import org.contineo.core.searchengine.search.ResultInterface;
import org.contineo.core.searchengine.search.Search;
import org.contineo.core.searchengine.search.SearchOptions;
import org.contineo.web.SessionManagement;
import org.contineo.web.StyleBean;
import org.contineo.web.document.DocumentRecord;
import org.contineo.web.i18n.Messages;
import org.contineo.web.navigation.NavigationBean;
import org.contineo.web.navigation.PageContentBean;

/**
 * A simple search form bean
 * 
 * @author Marco Meschieri - Logical Objects
 * @version $Id: SearchForm.java,v 1.10 2006/09/03 16:24:38 marco Exp $
 * @since 2.7
 */
public class SearchForm {
	protected static Log logger = LogFactory.getLog(SearchForm.class);

	private int hitsPerPage = 10;

	private int hitsPerBlock = hitsPerPage * 4;

	private int maxHits = hitsPerBlock;

	private String language = "all";

	private String query = "";

	private String phrase = "";

	private String any = "";

	private String nots = "";

	private String format = "all";

	private Integer sizeMin = null;

	private Integer sizeMax = null;

	private Date creationDateFrom;

	private Date creationDateTo;

	private Date sourceDateFrom;

	private Date sourceDateTo;

	private boolean fuzzy = false;

	private boolean content = true;

	private boolean keywords = true;

	private boolean source = false;

	private boolean sourceAuthor = false;

	private boolean sourceType = false;

	private boolean coverage = false;
	
	private boolean name = true;

	private Collection<DocumentResult> documentResult = new ArrayList<DocumentResult>();

	private Collection<Result> similar = new ArrayList<Result>();

	private NavigationBean navigation;

	private Search lastSearch = null;

	public SearchForm() {
		setQuery(Messages.getMessage("search") + "...");
		setLanguage(SessionManagement.getLanguage());
	}

	public String getLanguage() {
		return language;
	}

	public static Log getLogger() {
		return logger;
	}

	public static void setLogger(Log logger) {
		SearchForm.logger = logger;
	}

	public String getAny() {
		return any;
	}

	public void setAny(String any) {
		this.any = any;
	}

	public boolean isContent() {
		return content;
	}

	public void setContent(boolean content) {
		this.content = content;
	}

	public boolean isCoverage() {
		return coverage;
	}

	public boolean isName() {
		return name;
	}
	
	public void setCoverage(boolean coverage) {
		this.coverage = coverage;
	}

	public void setName(boolean name) {
		this.name = name;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isFuzzy() {
		return fuzzy;
	}

	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
	}

	public boolean isKeywords() {
		return keywords;
	}

	public void setKeywords(boolean keywords) {
		this.keywords = keywords;
	}

	public String getNots() {
		return nots;
	}

	public void setNots(String not) {
		this.nots = not;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}

	public boolean isSourceAuthor() {
		return sourceAuthor;
	}

	public void setSourceAuthor(boolean sourceAuthor) {
		this.sourceAuthor = sourceAuthor;
	}

	public boolean isSourceType() {
		return sourceType;
	}

	public void setSourceType(boolean sourceType) {
		this.sourceType = sourceType;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setDocumentResult(Collection<DocumentResult> result) {
		this.documentResult = result;
	}

	public void setSimilar(Collection<Result> similar) {
		this.similar = similar;
	}

	public int getHitsPerPage() {
		return hitsPerPage;
	}

	public void setHitsPerPage(int hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
	}

	public List<DocumentResult> getDocumentResult() {
		return new ArrayList<DocumentResult>(documentResult);
	}

	/**
	 * Returns the results as a map, the key is menuId
	 * 
	 * @return
	 */
	public Map<String, Result> getResultMap() {
		Map<String, Result> map = new HashMap<String, Result>();
		for (Result result : lastSearch.getResults()) {
			map.put(Long.toString(result.getMenuId()), result);
		}
		return map;
	}

	public Collection<Result> getSimilar() {
		return similar;
	}

	public String quickSearch() {
		phrase = "";
		any = "";
		nots = "";
		format = "all";
		fuzzy = false;
		content = true;
		keywords = true;
		source = false;
		sourceAuthor = false;
		sourceType = false;
		coverage = false;
		name=true;
		creationDateFrom = null;
		creationDateTo = null;
		sourceDateFrom = null;
		sourceDateTo = null;
		sizeMin = null;
		sizeMax = null;

		maxHits = hitsPerBlock;
		return searchHits();
	}

	/**
	 * Launches the search
	 */
	public String search() {
		maxHits = hitsPerBlock;
		return searchHits();
	}

	/**
	 * Execute the search.
	 * <p>
	 * <b>Note:</b> only the first maxHits will be returned
	 */
	public String searchHits() {
		if (SessionManagement.isValid()) {
			try {
				String username = SessionManagement.getUsername();

				SearchOptions opt = new SearchOptions();
				ArrayList<String> fields = new ArrayList<String>();

				if (isContent()) {
					fields.add("content");
				}

				if (isKeywords()) {
					fields.add("keywords");
				}

				if (isSource()) {
					fields.add("source");
				}

				if (isSourceAuthor()) {
					fields.add("sourceauthor");
				}

				if (isSourceType()) {
					fields.add("sourcetype");
				}

				if (isCoverage()) {
					fields.add("coverage");
				}

				if (isName()) {
					fields.add("name");
				}
				
				String[] flds = (String[]) fields.toArray(new String[fields.size()]);
				opt.setFields(flds);

				ArrayList<String> languages = new ArrayList<String>();

				if ("all".equals(language)) {
					List<String> iso639_2Languages = LanguageManager.getInstance().getISO639_2Languages();
					languages.addAll(iso639_2Languages);
				} else {
					languages.add(language);
				}

				String[] langs = (String[]) languages.toArray(new String[languages.size()]);
				opt.setLanguages(langs);
				
				opt.setFuzzy(isFuzzy());
				opt.setQueryStr(getQuery(), getPhrase(), getAny(), getNots());
				opt.setFormat(getFormat());
				opt.setCreationDateFrom(getCreationDateFrom());
				opt.setCreationDateTo(getCreationDateTo());
				opt.setSourceDateFrom(getSourceDateFrom());
				opt.setSourceDateTo(getSourceDateTo());
				opt.setLengthMin(getSizeMin());
				opt.setLengthMax(getSizeMax());
				opt.setUsername(username);

				String searchLanguage = "all".equals(language) ? SessionManagement.getLanguage() : language;
				lastSearch = new Search(opt, searchLanguage);
				lastSearch.setMaxHits(maxHits);

				List<Result> result = lastSearch.search();

				List<DocumentResult> docResult = new ArrayList<DocumentResult>();
				for (Result myResult : result) {
					DocumentResult dr = new DocumentResult(myResult);
					docResult.add(dr);
				}
				setDocumentResult(docResult);

				PageContentBean page = new PageContentBean("result", "search/result");
				page.setIcon(StyleBean.getImagePath("search.png"));
				page.setContentTitle(Messages.getMessage("msg.jsp.searchresult"));
				navigation.setSelectedPanel(page);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				Messages.addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
			}
		} else {
			return "login";
		}

		return null;
	}

	/**
	 * Relaunches the search includint one more block of hits
	 */
	public String searchMore() {
		maxHits += hitsPerBlock;

		return searchHits();
	}

	/**
	 * Search for similar documents
	 */
	public String searchSimilar() {
		if (SessionManagement.isValid()) {
			Map map = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			int menuId;

			if (map.containsKey("entry")) {
				ResultInterface entry = (ResultInterface) map.get("entry");
				menuId = entry.getMenuId();
			} else {
				DocumentRecord entry = (DocumentRecord) map.get("documentRecord");
				menuId = entry.getMenuId();
			}

			String username = SessionManagement.getUsername();

			try {
				Searcher searcher = new Searcher();
				similar = searcher.findSimilarDocuments(menuId, 0.0d, username);

				PageContentBean page = new PageContentBean("similar", "search/similar");
				page.setContentTitle(Messages.getMessage("msg.jsp.similardocs"));

				page.setIcon(StyleBean.getImagePath("similar.png"));
				navigation.setSelectedPanel(page);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				Messages.addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
			}
		} else {
			return "login";
		}

		return null;
	}
	
	
	/**
	 * Search for similar documents
	 */
	public String showDocumentPath() {
		if (SessionManagement.isValid()) {
			Map map = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			
			if (map.containsKey("entry")) {
				DocumentResult entry = (DocumentResult) map.get("entry");
				entry.showDocumentPath();
			} 

		} else {
			return "login";
		}

		return null;
	}
	

	/**
	 * Cleans up the resources used by this class. This method could be called
	 * when a session destroyed event is called.
	 */
	public void dispose() {
		documentResult.clear();
	}

	public void reset() {
		setQuery(Messages.getMessage("search") + "...");
		phrase = "";
		any = "";
		nots = "";
		format = "all";
		creationDateFrom = null;
		creationDateTo = null;
		sourceDateFrom = null;
		sourceDateTo = null;
		sizeMin = null;
		sizeMax = null;
		fuzzy = false;
		content = false;
		keywords = false;
		source = false;
		sourceAuthor = false;
		sourceType = false;
		coverage = false;
		name=true;
	}

	/**
	 * Shows the advanced search form
	 */
	public String advanced() {
		if ((Messages.getMessage("search") + "...").equals(query)) {
			setQuery("");
		}

		PageContentBean page = new PageContentBean("advancedSearch", "search/advancedSearch");
		page.setContentTitle(Messages.getMessage("search.advanced"));

		page.setIcon(StyleBean.getImagePath("extsearch.gif"));
		navigation.setSelectedPanel(page);

		return null;
	}

	public void setNavigation(NavigationBean navigation) {
		this.navigation = navigation;
	}

	public boolean isMoreHitsPresent() {
		return lastSearch.isMoreHitsPresent();
	}

	public int getHitsPerBlock() {
		return hitsPerBlock;
	}

	public int getEstimatedHitsNumber() {
		return lastSearch.getEstimatedHitsNumber();
	}

	public long getExecTime() {
		return lastSearch.getExecTime();
	}

	public Date getCreationDateFrom() {
		return creationDateFrom;
	}

	public void setCreationDateFrom(Date creationDateFrom) {
		this.creationDateFrom = creationDateFrom;
	}

	public Date getCreationDateTo() {
		return creationDateTo;
	}

	public Integer getSizeMin() {
		return sizeMin;
	}

	public void setSizeMin(Integer sizeMin) {
		this.sizeMin = sizeMin;
	}

	public Integer getSizeMax() {
		return sizeMax;
	}

	public void setSizeMax(Integer sizeMax) {
		this.sizeMax = sizeMax;
	}

	public Date getSourceDateFrom() {
		return sourceDateFrom;
	}

	public void setSourceDateFrom(Date sourceDateFrom) {
		this.sourceDateFrom = sourceDateFrom;
	}

	public Date getSourceDateTo() {
		return sourceDateTo;
	}

	public void setSourceDateTo(Date sourceDateTo) {
		if (sourceDateTo != null) {
			// Include all the specified day
			Calendar cal = Calendar.getInstance();
			cal.setTime(sourceDateTo);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.HOUR, 23);
			this.sourceDateTo = cal.getTime();
		} else {
			this.sourceDateTo = sourceDateTo;
		}
	}

	public void setCreationDateTo(Date creationDateTo) {
		if (creationDateTo != null) {
			// Include all the specified day
			Calendar cal = Calendar.getInstance();
			cal.setTime(creationDateTo);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.HOUR, 23);
			this.creationDateTo = cal.getTime();
		} else {
			this.creationDateTo = creationDateTo;
		}
	}
}