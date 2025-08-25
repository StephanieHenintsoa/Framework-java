package model.authentification;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe utilitaire pour gérer l'authentification et l'autorisation.
 */
public class AuthenticationManager {

    private static final String USER_SESSION_KEY = "authenticatedUser";
    private static final String ROLES_SESSION_KEY = "userRoles";

    /**
     * Vérifie si un utilisateur est connecté.
     */
    public static boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(USER_SESSION_KEY) != null;
    }

    /**
     * Connecte un utilisateur et enregistre ses rôles.
     */
    public static void login(HttpSession session, Object user, Set<Class<?>> roles) {
        if (session != null) {
            session.setAttribute(USER_SESSION_KEY, user);
            session.setAttribute(ROLES_SESSION_KEY, new HashSet<>(roles));
        }
    }

    /**
     * Déconnecte l'utilisateur.
     */
    public static void logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_SESSION_KEY);
            session.removeAttribute(ROLES_SESSION_KEY);
            session.invalidate();
        }
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle donné.
     */
    @SuppressWarnings("unchecked")
    public static boolean hasRole(HttpSession session, Class<?> requiredRole) {
        if (session == null) return false;
        Set<Class<?>> roles = (Set<Class<?>>) session.getAttribute(ROLES_SESSION_KEY);
        return roles != null && roles.contains(requiredRole);
    }

    /**
     * Récupère l'objet utilisateur actuellement connecté.
     */
    public static Object getCurrentUser(HttpSession session) {
        return (session != null) ? session.getAttribute(USER_SESSION_KEY) : null;
    }
}
