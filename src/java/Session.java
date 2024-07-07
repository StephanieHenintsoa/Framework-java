package utils;
import javax.servlet.http.HttpSession;

public class Session {
    private HttpSession session;

    public Session(HttpSession session) {
        this.session = session;
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void add(String key, Object object) {
        session.setAttribute(key, object);
    }

    public void delete(String key) {
        session.removeAttribute(key);
    }
    public void add(String key, Object[] object) {
        session.setAttribute(key, object);
    }
}
