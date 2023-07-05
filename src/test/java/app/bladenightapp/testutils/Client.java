package app.bladenightapp.testutils;

import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.EventMessage;
import app.bladenight.common.network.messages.*;
import app.bladenight.common.network.messages.EventMessage.EventStatus;
import app.bladenight.server.BladenightWampServerMain;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class Client {
    public Client(BladenightWampServerMain server) {
        this.channel = new ProtocollingChannel();
        this.server = server;

         this.session = new Session() {
             @Override
             public void close() {

             }

             @Override
             public void close(CloseStatus closeStatus) {

             }

             @Override
             public void close(int statusCode, String reason) {

             }

             @Override
             public void disconnect() throws IOException {

             }

             @Override
             public long getIdleTimeout() {
                 return 0;
             }

             @Override
             public InetSocketAddress getLocalAddress() {
                 return new InetSocketAddress(10091);
             }

             @Override
             public WebSocketPolicy getPolicy() {
                 return null;
             }

             @Override
             public String getProtocolVersion() {
                 return "2";
             }

             @Override
             public RemoteEndpoint getRemote() {
                 return new RemoteEndpoint() {
                     @Override
                     public void sendBytes(ByteBuffer data) throws IOException {

                     }

                     @Override
                     public Future<Void> sendBytesByFuture(ByteBuffer data) {
                         return null;
                     }

                     @Override
                     public void sendBytes(ByteBuffer data, WriteCallback callback) {

                     }

                     @Override
                     public void sendPartialBytes(ByteBuffer fragment, boolean isLast) throws IOException {

                     }

                     @Override
                     public void sendPartialString(String fragment, boolean isLast) throws IOException {

                     }

                     @Override
                     public void sendPing(ByteBuffer applicationData) throws IOException {

                     }

                     @Override
                     public void sendPong(ByteBuffer applicationData) throws IOException {

                     }

                     @Override
                     public void sendString(String text) throws IOException {

                     }

                     @Override
                     public Future<Void> sendStringByFuture(String text) {
                         return null;
                     }

                     @Override
                     public void sendString(String text, WriteCallback callback) {

                     }

                     @Override
                     public BatchMode getBatchMode() {
                         return null;
                     }

                     @Override
                     public void setBatchMode(BatchMode mode) {

                     }

                     @Override
                     public int getMaxOutgoingFrames() {
                         return 0;
                     }

                     @Override
                     public void setMaxOutgoingFrames(int maxOutgoingFrames) {

                     }

                     @Override
                     public InetSocketAddress getInetSocketAddress() {
                         return null;
                     }

                     @Override
                     public void flush() throws IOException {

                     }
                 };
             }

             @Override
             public InetSocketAddress getRemoteAddress() {
                 return new InetSocketAddress(0);
             }

             @Override
             public UpgradeRequest getUpgradeRequest() {
                 return new UpgradeRequest() {
                     @Override
                     public void addExtensions(ExtensionConfig... configs) {

                     }

                     @Override
                     public void addExtensions(String... configs) {

                     }

                     @Override
                     public void clearHeaders() {

                     }

                     @Override
                     public List<HttpCookie> getCookies() {
                         return null;
                     }

                     @Override
                     public List<ExtensionConfig> getExtensions() {
                         return null;
                     }

                     @Override
                     public String getHeader(String name) {
                         return null;
                     }

                     @Override
                     public int getHeaderInt(String name) {
                         return 0;
                     }

                     @Override
                     public Map<String, List<String>> getHeaders() {
                         return null;
                     }

                     @Override
                     public List<String> getHeaders(String name) {
                         return null;
                     }

                     @Override
                     public String getHost() {
                         return null;
                     }

                     @Override
                     public String getHttpVersion() {
                         return null;
                     }

                     @Override
                     public String getMethod() {
                         return null;
                     }

                     @Override
                     public String getOrigin() {
                         return null;
                     }

                     @Override
                     public Map<String, List<String>> getParameterMap() {
                         return null;
                     }

                     @Override
                     public String getProtocolVersion() {
                         return null;
                     }

                     @Override
                     public String getQueryString() {
                         return null;
                     }

                     @Override
                     public URI getRequestURI() {
                         try {
                             return new URI("wss","localhost","/","fragment");
                         } catch (URISyntaxException e) {
                             throw new RuntimeException(e);
                         }
                     }

                     @Override
                     public Object getSession() {
                         return null;
                     }

                     @Override
                     public List<String> getSubProtocols() {
                         return null;
                     }

                     @Override
                     public Principal getUserPrincipal() {
                         return null;
                     }

                     @Override
                     public boolean hasSubProtocol(String test) {
                         return false;
                     }

                     @Override
                     public boolean isOrigin(String test) {
                         return false;
                     }

                     @Override
                     public boolean isSecure() {
                         return false;
                     }

                     @Override
                     public void setCookies(List<HttpCookie> cookies) {

                     }

                     @Override
                     public void setExtensions(List<ExtensionConfig> configs) {

                     }

                     @Override
                     public void setHeader(String name, List<String> values) {

                     }

                     @Override
                     public void setHeader(String name, String value) {

                     }

                     @Override
                     public void setHeaders(Map<String, List<String>> headers) {

                     }

                     @Override
                     public void setHttpVersion(String httpVersion) {

                     }

                     @Override
                     public void setMethod(String method) {

                     }

                     @Override
                     public void setRequestURI(URI uri) {

                     }

                     @Override
                     public void setSession(Object session) {

                     }

                     @Override
                     public void setSubProtocols(List<String> protocols) {

                     }

                     @Override
                     public void setSubProtocols(String... protocols) {

                     }
                 };
             }

             @Override
             public UpgradeResponse getUpgradeResponse() {
                 return new UpgradeResponse() {
                     @Override
                     public void addHeader(String name, String value) {

                     }

                     @Override
                     public String getAcceptedSubProtocol() {
                         return null;
                     }

                     @Override
                     public List<ExtensionConfig> getExtensions() {
                         return null;
                     }

                     @Override
                     public String getHeader(String name) {
                         return "testheader123456";
                     }

                     @Override
                     public Set<String> getHeaderNames() {
                         return null;
                     }

                     @Override
                     public Map<String, List<String>> getHeaders() {
                         return null;
                     }

                     @Override
                     public List<String> getHeaders(String name) {
                         return null;
                     }

                     @Override
                     public int getStatusCode() {
                         return 0;
                     }

                     @Override
                     public String getStatusReason() {
                         return null;
                     }

                     @Override
                     public boolean isSuccess() {
                         return false;
                     }

                     @Override
                     public void sendForbidden(String message) throws IOException {

                     }

                     @Override
                     public void setAcceptedSubProtocol(String protocol) {

                     }

                     @Override
                     public void setExtensions(List<ExtensionConfig> extensions) {

                     }

                     @Override
                     public void setHeader(String name, String value) {

                     }

                     @Override
                     public void setStatusCode(int statusCode) {

                     }

                     @Override
                     public void setStatusReason(String statusReason) {

                     }

                     @Override
                     public void setSuccess(boolean success) {

                     }
                 };
             }

             @Override
             public boolean isOpen() {
                 return true;
             }

             @Override
             public boolean isSecure() {
                 return false;
             }

             @Override
             public void setIdleTimeout(long ms) {

             }

             @Override
             public SuspendToken suspend() {
                 return null;
             }
         };
         server.registerSession(session, channel);
    }

    public RealTimeUpdateData getRealtimeUpdate(GpsInfo gpsInfo) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_REALTIME_UPDATE.getText());
        msg.setPayload(gpsInfo);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        Message message = MessageMapper.fromJson(channel.lastMessage());
        assertSame(message.getType(), MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(RealTimeUpdateData.class);
    }

    public EventMessage getActiveEvent() throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_ACTIVE_EVENT.getText());
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        Message message = MessageMapper.fromJson(channel.lastMessage());
        assert(message.getType() == MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(EventMessage.class);
    }

    public RouteMessage getActiveRoute() throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_ACTIVE_ROUTE.getText());
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        Message message = MessageMapper.fromJson(channel.lastMessage());
        assertSame(message.getType(), MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(RouteMessage.class);
    }

    public EventListMessage getAllEvents() throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_ALL_EVENTS.getText());
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        Message message = MessageMapper.fromJson(channel.lastMessage());
        assertSame(message.getType(), MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(EventListMessage.class);
    }

    public Message getAllRouteNames() throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_ALL_ROUTE_NAMES.getText());
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }


    public Message sendRelationshipRequest(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.CREATE_RELATIONSHIP.getText());
        RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, requestId);
        msg.setPayload(partnershipMessage);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    public Message getFriends(String deviceId) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_FRIENDS.getText());
        msg.setPayload(deviceId);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    public Message deleteReleationship(String deviceId, int friendId) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.DELETE_RELATIONSHIP.getText());
        RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, 0);
        msg.setPayload(partnershipMessage);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    public Message setActiveRouteTo(String newRoute, String password) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.SET_ACTIVE_ROUTE.getText());
        SetActiveRouteMessage payload = new SetActiveRouteMessage(newRoute, password);
        assertTrue(payload.verify(password, 10000));
        payload.setRouteName(newRoute);
        msg.setPayload(payload);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    public Message setActiveStatusTo(EventStatus newStatus, String password) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.SET_ACTIVE_STATUS.getText());

        SetActiveStatusMessage payload = new SetActiveStatusMessage(newStatus, password);
        assertTrue(payload.verify(password, 10000));

        msg.setPayload(payload);

        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    public boolean verifyPasswordAgainstServer(String password) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.VERIFY_ADMIN_PASSWORD.getText());
        AdminMessage adminMessage = new AdminMessage();
        adminMessage.authenticate(password);
        msg.setPayload(adminMessage);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        Message returnedMessage = MessageMapper.fromJson(channel.lastMessage());
        return MessageType.RESULT.equals(returnedMessage.getType());
    }


    public Message shakeHands(String deviceId, int buildNumber, String phoneManufacturer, String phoneModel, String androidRelease) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.SHAKE_HANDS.getText());
        HandshakeClientMessage handshakeClientMessage = new HandshakeClientMessage(deviceId, buildNumber, phoneManufacturer, phoneModel, androidRelease);
        msg.setPayload(handshakeClientMessage);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }


    public Message getRoute(String routeName) throws IOException, BadArgumentException {
        int messageCount = channel.handledMessages.size();
        String callId = UUID.randomUUID().toString();
        CallMessage msg = new CallMessage(callId, BladenightUrl.GET_ROUTE.getText());
        msg.setPayload(routeName);
        server.handleIncomingMessage(session, msg);
        assertEquals(messageCount + 1, channel.handledMessages.size());
        return MessageMapper.fromJson(channel.lastMessage());
    }

    private final ProtocollingChannel channel;
    private final Session session;
    private final BladenightWampServerMain server;
}
