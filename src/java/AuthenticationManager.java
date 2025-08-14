package authentication;

import javax.servlet.http.HttpSession;

public class AuthenticationManager {
    public static final String SESSION_USER_KEY = "currentUser";
    
    public static boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(SESSION_USER_KEY) != null;
    }
    
    public static boolean hasRole(HttpSession session, Class<?> requiredRole) {
        Object user = session.getAttribute(SESSION_USER_KEY);
        if (user == null) return false;
        return user.getClass().equals(requiredRole);
    }
    
    public static void setAuthenticatedUser(HttpSession session, Object user) {
        session.setAttribute(SESSION_USER_KEY, user);
    }
    
    public static void clearAuthentication(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
    }
    
    public static Object getAuthenticatedUser(HttpSession session) {
        return session.getAttribute(SESSION_USER_KEY);
    }
}
