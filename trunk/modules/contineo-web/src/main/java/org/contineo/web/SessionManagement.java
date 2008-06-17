package org.contineo.web;

import org.contineo.core.security.User;
import org.contineo.core.security.dao.UserDAO;

import org.contineo.util.Context;

import org.contineo.web.util.Constants;

import java.util.Map;

import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;


/**
 *
 * @author Michael Scholz
 */
public class SessionManagement {
    public static boolean isValid(HttpSession session) {
        boolean result = true;
        String username = (String) session.getAttribute(Constants.AUTH_USERNAME);

        if ((username == null) || username.equals("")) {
            result = false;
        }

        if (session.isNew()) {
            result = false;
        }

        return result;
    }

    public static boolean isValid() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                                                        .getSession(false);

        return isValid(session);
    }

    public static String getUsername() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        String username = (String) session.get(Constants.AUTH_USERNAME);

        return username;
    }

    public static User getUser() {
        String username = getUsername();
        UserDAO dao = (UserDAO) Context.getInstance().getBean(UserDAO.class);
        User user=dao.findByPrimaryKey(username);
        user.initGroupNames();
        return user;
    }

    public static String getLanguage() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map session = facesContext.getExternalContext().getSessionMap();
        String language = (String) session.get(Constants.LANGUAGE);

        return language;
    }
}
