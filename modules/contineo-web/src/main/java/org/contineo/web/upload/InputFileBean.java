package org.contineo.web.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventObject;

import javax.faces.event.ActionEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.web.SessionManagement;

import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.component.inputfile.InputFile;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

/**
 * <p>
 * The InputFileBean class is the backing bean for the inputfile showcase
 * demonstration. It is used to store the state of the uploaded file.
 * </p>
 * 
 * @author Marco Meschieri
 * @version $Id: InputFileBean.java,v 1.4 2007/07/16 06:35:17 marco Exp $
 * @since 3.0
 */
public class InputFileBean implements Renderable {
	protected static Log log = LogFactory.getLog(InputFileBean.class);

	private int percent = -1;

	private String language;

	private File file = null;

	private String description;

	private String versionType;

	private PersistentFacesState state;

	private RenderManager renderManager;

	private String fileName = "";

	private String contentType = "";

	private boolean extractKeywords = true;

	public InputFileBean() {
		state = PersistentFacesState.getInstance();
	}

	/**
	 * Sets the Render Manager.
	 * 
	 * @param renderManager
	 */
	public void setRenderManager(RenderManager renderManager) {
		this.renderManager = renderManager;
	}

	/**
	 * Gets RenderManager, just try to satisfy WAS
	 * 
	 * @return RenderManager null
	 */
	public RenderManager getRenderManager() {
		return null;
	}

	/**
	 * Get the PersistentFacesState.
	 * 
	 * @return state the PersistantFacesState
	 */
	public PersistentFacesState getState() {
		return state;
	}

	/**
	 * Handles rendering exceptions for the progress bar.
	 * 
	 * @param renderingException the exception that occured
	 */
	public void renderingException(RenderingException renderingException) {
		renderingException.printStackTrace();
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public int getPercent() {
		return percent;
	}

	public void setFile(File file) {
		try {
			// If another file was uploaded, first delete the old file
			if ((this.file != null) && this.file.exists()) {
				FileUtils.forceDelete(this.file);
			}
		} catch (IOException e) {
			log.error("Unable to delete temp file " + this.file);
		}

		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void action(ActionEvent event) {
		InputFile inputFile = (InputFile) event.getSource();
		fileName = inputFile.getFileInfo().getFileName();
		contentType = inputFile.getFileInfo().getContentType();
		this.percent = inputFile.getFileInfo().getPercent();

		if (inputFile.getStatus() == InputFile.SAVED) {
			setFile(inputFile.getFile());
		}

		if (inputFile.getStatus() == InputFile.INVALID) {
			inputFile.getFileInfo().getException().printStackTrace();
		}

		if (inputFile.getStatus() == InputFile.SIZE_LIMIT_EXCEEDED) {
			inputFile.getFileInfo().getException().printStackTrace();
		}

		if (inputFile.getStatus() == InputFile.UNKNOWN_SIZE) {
			inputFile.getFileInfo().getException().printStackTrace();
		}
	}

	public void progress(EventObject event) {
		InputFile file = (InputFile) event.getSource();
		this.percent = file.getFileInfo().getPercent();

		if (renderManager != null) {
			renderManager.requestRender(this);
		}

		try {
			if (state != null) {
				state.render();
			}
		} catch (RenderingException e) {
		}
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean isReady() {
		return percent == 100;
	}

	public void deleteUploadDir() {
		log.debug("file = " + file);
		if ((file != null) && file.exists()) {
			try {
				FileUtils.forceDelete(file.getParentFile());
				file = null;
			} catch (IOException e) {
				log.error("Unable to delete temp file " + file.getPath());
			}
		}
		percent = 0;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Reads the uploaded file and stores it's content into a string bufferO
	 * 
	 * @throws IOException
	 */
	public StringBuffer getContent() throws IOException {
		byte[] buffer = new byte[1024];
		int read = 0;
		StringBuffer content = new StringBuffer();
		InputStream in = new FileInputStream(getFile());

		while ((read = in.read(buffer, 0, 1024)) >= 0) {
			content.append(new String(buffer, 0, read));
		}

		return content;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void reset() {
		deleteUploadDir();
		this.description = null;
		this.language = SessionManagement.getLanguage();
	}

	public String getVersionType() {
		return versionType;
	}

	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	public boolean isExtractKeywords() {
		return extractKeywords;
	}

	public void setExtractKeywords(boolean extractKeywords) {
		this.extractKeywords = extractKeywords;
	}
}