package app.bladenight.server;

import app.bladenight.common.keyvaluestore.KeyValueStoreSingleton;
import app.bladenight.wampv2.server.WampBnServer;
import app.bladenight.wampv2.server.WampBnServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.*;

import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;


public class BladenightSocketServletMain extends WebSocketServlet {
    private static WampBnServer wampBnServer = new WampBnServerImpl();
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String BASIC_REALM = "Basic realm=\"api\"";
    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    private static final long ASYNC_TIMEOUT = 10 * 60 * 1000;
    private static Logger logger = LogManager.getLogger(BladenightSocketServletAux.class);

    private HttpServletRequest request;

    public BladenightSocketServletMain() {
    }

    public BladenightSocketServletMain(WampBnServer wampBnServer) {
        this.wampBnServer = wampBnServer;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new BnWebSocketCreator());
    }

    public class BnWebSocketCreator implements WebSocketCreator {
        @Override
        public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
            try {
                if (!authenticate(servletUpgradeRequest)) {
                    servletUpgradeResponse.sendForbidden("App not allowed");
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
            return new BladenightJettyServerWebSocket(wampBnServer);
        }
    }

    private boolean authenticate(ServletUpgradeRequest req) {
        if (wampBnServer.isBanned(req)) {
            return false;
        }
        String appcredentialsKey = "bnserver.security.appcredentials.main";
        String appcredentials = KeyValueStoreSingleton.getString(appcredentialsKey, "");
        if (appcredentials.length() == 0) return true;

        request = req.getHttpServletRequest();

        if (req.getHttpServletRequest().getMethod().equals("OPTIONS")) {
            return false;
        }
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null) {
            try {
                String authKey=authHeader;
                if (authHeader.toLowerCase().startsWith("basic ")) authKey=authHeader.substring(6).trim();
                String regex = "^[^\\/\\/]*\\/\\/";
                String pair = new String(Base64.getDecoder().decode(authKey.replaceAll(regex, "")));
                if (appcredentials.equals(pair)) {
                    return true;
                }   if (appcredentials.equals(pair)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("creatwebsocket failed authheader ="+authHeader, e);
                return false;
            }
        }
        wampBnServer.addToBanned(req);
        return false;
    }
}
