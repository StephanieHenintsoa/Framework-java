
package guard;

import annotation.Auth;
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