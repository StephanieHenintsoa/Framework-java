
package guard;

import annotation.Auth;
import authentication.AuthenticationManager;
import model.authentification.AuthenticationManager;
import authentication.AuthenticationConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class AuthGuard {
    public static boolean checkAuthentication(HttpServletRequest request, HttpServletResponse response, 
        Class<?> controllerClass, Method method) throws Exception {
            
            Auth controllerAuth = controllerClass.getAnnotation(Auth.class);
            Auth methodAuth = method.getAnnotation(Auth.class);

            // Pas besoin d'auth si aucune annotation
            if (controllerAuth == null && methodAuth == null) {
                return true;
            }

            // Vérification de session expirée
            if (request.getSession(false) == null) {
                request.getSession(true).setAttribute("auth_error", "Votre session a expiré, veuillez vous reconnecter.");
                response.sendRedirect(request.getContextPath() + AuthenticationConfig.getLoginPage());
                return false;
            }

            // Vérification utilisateur connecté
            if (!AuthenticationManager.isAuthenticated(request.getSession())) {
                request.getSession().setAttribute("auth_error", "Vous devez être connecté pour accéder à cette ressource.");
                response.sendRedirect(request.getContextPath() + AuthenticationConfig.getLoginPage());
                return false;
            }

            // Récupère l'annotation applicable (méthode prioritaire)
            Auth auth = methodAuth != null ? methodAuth : controllerAuth;
            Class<?> requiredRole = auth.value();

            // Vérification des rôles
            if (!requiredRole.equals(Object.class)) {
                if (!AuthenticationManager.hasRole(request.getSession(), requiredRole)) {
                    request.getSession().setAttribute("auth_error", "Accès refusé : vous n’avez pas les droits nécessaires.");
                    response.sendRedirect(request.getContextPath() + AuthenticationConfig.getUnauthorizedPage());
                    return false;
                }
            }

            // (Optionnel) : journaliser l’accès
            System.out.println("✅ Accès autorisé : " + request.getRequestURI() + " par " 
                + request.getSession().getAttribute("username"));

            return true;
        }

=======
    
    public static boolean checkAuthentication(HttpServletRequest request, HttpServletResponse response, 
            Class<?> controllerClass, Method method) throws Exception {
        
        Auth controllerAuth = controllerClass.getAnnotation(Auth.class);
        Auth methodAuth = method.getAnnotation(Auth.class);
        
        if (controllerAuth == null && methodAuth == null) {
            return true;
        }
        
        if (!AuthenticationManager.isAuthenticated(request.getSession())) {
            response.sendRedirect(request.getContextPath() + AuthenticationConfig.getLoginPage());
            return false;
        }
        
        Auth auth = methodAuth != null ? methodAuth : controllerAuth;
        Class<?> requiredRole = auth.value();
        
        if (!requiredRole.equals(Object.class)) {
            if (!AuthenticationManager.hasRole(request.getSession(), requiredRole)) {
                response.sendRedirect(request.getContextPath() + AuthenticationConfig.getUnauthorizedPage());
                return false;
            }
        }
        
        return true;
    }
}