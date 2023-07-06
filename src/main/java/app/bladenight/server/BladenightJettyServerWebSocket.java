package app.bladenight.server;

import app.bladenight.wampv2.server.WampBnServer;
import app.bladenight.wampv2.server.WampBnServerImpl;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;

/***
 * Proxy between a specific WebSocket session on the server side and the internal WampServer.
 */
@WebSocket
public class BladenightJettyServerWebSocket {
    protected WampBnServer wampBnServer;
    private static final Logger logger = LogManager.getLogger(BladenightJettyServerWebSocket.class);

    public BladenightJettyServerWebSocket() {
        wampBnServer = new WampBnServerImpl();
    }

    public BladenightJettyServerWebSocket(WampBnServer wampBnServer) {
        this.wampBnServer = wampBnServer;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        session = wampBnServer.registerSession(session, null);
        logger.debug(String.format("WebSocket onOpen Connect: %s Protocol: %s", session.getRemoteAddress(), session.getProtocolVersion()));
    }

    @OnWebSocketClose
    public void onClose(Session session, int closeCode, String reason) {
        wampBnServer.closeSession(session, closeCode, reason);
        logger.debug("WAMP session closed. Reason: " + reason + " Session:" + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String data) {
        try {
            logger.debug("onMessage:" + data);
            wampBnServer.handleIncomingString(session, data);

        } catch (IOException e) {
            logger.warn("Got IOException in onMessage:" + data, e);
        } catch (BadArgumentException e) {
            logger.warn("Got BadArgument on message:" + data);
        }
    }

    @OnWebSocketError
    public void onWebSocketError(Session session, Throwable error) {

        if (error instanceof org.eclipse.jetty.websocket.api.CloseException) {
            logger.debug("Websocket timeout " + session.getRemoteAddress() + " Error: " + error.getLocalizedMessage());
        } else if (error instanceof org.eclipse.jetty.io.EofException) {
            logger.debug("Broken Pipe " + error.getLocalizedMessage());
        } else {
            logger.error("Websocket error", error);
        }
    }


}
