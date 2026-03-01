package com.kripal.hostel.service;

import com.kripal.hostel.dao.UserDAO;
import com.kripal.hostel.model.User;

/**
 * Authenticates users and returns their role.
 */
public class LoginService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * @return "ADMIN" or "USER" on success, null on failure.
     */
    public String authenticate(String username, String password) {
        User user = userDAO.login(username, password);
        return (user != null) ? user.getRole() : null;
    }

    /** Returns the full User object (for display purposes). */
    public User getUser(String username, String password) {
        return userDAO.login(username, password);
    }
}
