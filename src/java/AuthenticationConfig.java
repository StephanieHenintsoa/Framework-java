
package authentication;

public class AuthenticationConfig {
    private static String loginPage = "/login";
    private static String unauthorizedPage = "/unauthorized";
    
    public static void setLoginPage(String page) {
        loginPage = page;
    }
    
    public static String getLoginPage() {
        return loginPage;
    }
    
    public static void setUnauthorizedPage(String page) {
        unauthorizedPage = page;
    }
    
    public static String getUnauthorizedPage() {
        return unauthorizedPage;
    }
}