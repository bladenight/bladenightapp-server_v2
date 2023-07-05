package app.bladenight.server;

import app.bladenight.common.keyvaluestore.KeyValueStoreSingleton;
import app.bladenight.wampv2.server.WampBnServer;
import app.bladenight.wampv2.server.WampBnServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;


public class BladenightSocketServletAux extends WebSocketServlet {
    private static WampBnServer wampBnServer = new WampBnServerImpl();
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static Logger logger =  LogManager.getLogger(BladenightSocketServletAux.class);


    public BladenightSocketServletAux(WampBnServer wampBnServer) {
        BladenightSocketServletAux.wampBnServer = wampBnServer;
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
                logger.error("creatwebsocket failed",e);
                return null;
            }
            return new BladenightJettyServerWebSocket(wampBnServer);
        }
    }

    private boolean authenticate(ServletUpgradeRequest req) {
          if (wampBnServer.isBanned(req)) {
            return false;
        }
        //ws: Protocol does not supported Authenticationheader
        if (req.getRequestURI().getScheme().equals("ws")) return true;

        String appcredentialsKey = "bnserver.security.appcredentials.aux";
        String appcredentials = KeyValueStoreSingleton.getString(appcredentialsKey, "");
        if (appcredentials.length() == 0) return true;

        HttpServletRequest request = req.getHttpServletRequest();

        if (req.getHttpServletRequest().getMethod().equals("OPTIONS")) {
            return false;
        }
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null) {
            String authKey=authHeader;
            if (authHeader.toLowerCase().startsWith("basic ")) authKey=authHeader.substring(6).trim();
            String regex = "^[^\\/\\/]*\\/\\/";
            String pair = new String(Base64.getDecoder().decode(authKey.replaceAll(regex, "")));
            if (appcredentials.equals(pair)) {
                return true;
            }
        }
        wampBnServer.addToBanned(req);
        return false;
    }
}
