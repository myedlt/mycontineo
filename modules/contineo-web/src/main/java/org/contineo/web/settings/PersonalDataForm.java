package org.contineo.web.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.contineo.core.security.SecurityManager;
import org.contineo.core.security.User;
import org.contineo.core.security.dao.UserDAO;

import org.contineo.util.Context;

import org.contineo.web.SessionManagement;
import org.contineo.web.admin.UsersRecordsManager;
import org.contineo.web.i18n.Messages;

import javax.faces.context.FacesContext;


/**
 * Personal data editing
 *
 * @author Marco Meschieri
 * @version $Id: UserForm.java,v 1.1 2007/10/16 16:10:33 marco Exp $
 * @since 3.0
 */
public class PersonalDataForm {
    protected static Log log = LogFactory.getLog(PersonalDataForm.class);
    private String name;
    private String firstName;
    private String street;
    private String postalCode;
    private String city;
    private String country;
    private String language;
    private String email;
    private String phone;

    public PersonalDataForm() {
        super();

        User user = SessionManagement.getUser();
        name = user.getName();
        firstName = user.getFirstName();
        street = user.getStreet();
        postalCode = user.getPostalcode();
        city = user.getCity();
        country = user.getCountry();
        language = user.getLanguage();
        email = user.getEmail();
        phone = user.getTelephone();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String save() {
        if (SessionManagement.isValid()) {
            try {
                UserDAO dao = (UserDAO) Context.getInstance()
                                               .getBean(UserDAO.class);
                User user = SessionManagement.getUser();
                user.setFirstName(firstName);
                user.setName(name);
                user.setCity(city);
                user.setStreet(street);
                user.setCountry(country);
                user.setEmail(email);
                user.setLanguage(language);
                user.setPostalcode(postalCode);
                user.setTelephone(phone);

                SecurityManager manager = (SecurityManager) Context.getInstance()
                                                                   .getBean(SecurityManager.class);
                boolean stored = dao.store(user);

                if (!stored) {
                    Messages.addLocalizedError(
                        "errors.action.saveuser.notstored");
                } else {
                    Messages.addLocalizedInfo("msg.action.changeuser");
                }

                user.initGroupNames();

                UsersRecordsManager recordsManager = ((UsersRecordsManager) FacesContext.getCurrentInstance()
                                                                                        .getApplication()
                                                                                        .createValueBinding("#{usersRecordsManager}")
                                                                                        .getValue(FacesContext.getCurrentInstance()));
                recordsManager.reload();
                recordsManager.setSelectedPanel("list");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                Messages.addLocalizedError("errors.action.saveuser.notstored");
            }

            return null;
        } else {
            return "login";
        }
    }
}
