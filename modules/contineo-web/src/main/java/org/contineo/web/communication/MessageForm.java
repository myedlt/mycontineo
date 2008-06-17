package org.contineo.web.communication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.contineo.core.communication.SystemMessage;
import org.contineo.core.communication.dao.SystemMessageDAO;
import org.contineo.core.security.dao.UserDAO;

import org.contineo.util.Context;

import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;

import java.util.Date;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;


/**
 * A system message editing form
 *
 * @author Marco Meschieri - Logical Objects
 * @version $Id: MessageForm.java,v 1.2 2006/08/31 15:31:18 marco Exp $
 * @since ###release###
 */
public class MessageForm {
    protected static Log log = LogFactory.getLog(MessageForm.class);
    private SystemMessage message = new SystemMessage();
    private boolean readOnly = true;
    private boolean confirmation = false;

    public SystemMessage getMessage() {
        return message;
    }

    public void setMessage(SystemMessage message) {
        this.message = message;
        confirmation = message.getConfirmation() != 0;
    }

    public String back() {
        Application application = FacesContext.getCurrentInstance()
                                              .getApplication();
        MessagesRecordsManager manager = ((MessagesRecordsManager) application.createValueBinding(
                "#{messagesRecordsManager}")
                                                                              .getValue(FacesContext.getCurrentInstance()));
        manager.listMessages();

        return null;
    }

    public String insert() {
        return null;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean edit) {
        this.readOnly = edit;
    }

    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;

        if (confirmation && (message != null)) {
            message.setConfirmation(1);
        } else if (!confirmation && (message != null)) {
            message.setConfirmation(0);
        }
    }

    public String save() {
        if (SessionManagement.isValid()) {
            try {
                UserDAO udao = (UserDAO) Context.getInstance()
                                                .getBean(UserDAO.class);
                String recipient = message.getRecipient();

                if (udao.existsUser(recipient)) {
                    message.setAuthor(SessionManagement.getUsername());
                    message.setSentDate(String.valueOf(new Date().getTime()));

                    SystemMessageDAO smdao = (SystemMessageDAO) Context.getInstance()
                                                                       .getBean(SystemMessageDAO.class);
                    boolean stored = smdao.store(message);

                    if (!stored) {
                        Messages.addLocalizedError("errors.action.savesysmess");
                    } else {
                        Messages.addLocalizedInfo("msg.action.savesysmess");
                    }
                } else {
                    Messages.addLocalizedError("errors.action.usernotexists");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                Messages.addLocalizedError("errors.action.savesysmess");
            }
        } else {
            return "login";
        }

        return null;
    }
}
