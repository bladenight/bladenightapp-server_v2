package app.bladenight.server;

import app.bladenight.common.events.EventList;
import app.bladenight.common.imagesandlinks.ImagesAndLinksList;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.server.rpchandlers.*;
import app.bladenight.wampv2.server.WampBnServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class BladenightWampServerAux extends WampBnServerImpl {

    static public class Builder {

        Builder() {
            this.bladenightAuxWampServer = new BladenightWampServerAux();
        }

        public Builder setProcession(Procession procession) {
            bladenightAuxWampServer.procession = procession;
            return this;
        }

        public Builder setEventList(EventList eventList) {
            bladenightAuxWampServer.eventList = eventList;
            return this;
        }

        public Builder setRouteStore(RouteStore routeStore) {
            bladenightAuxWampServer.routeStore = routeStore;
            return this;
        }

        public Builder setImagesAndLinks(ImagesAndLinksList imagesAndLinkList) {
            bladenightAuxWampServer.imagesAndLinkList = imagesAndLinkList;
            return this;
        }

        public BladenightWampServerAux build() {
            bladenightAuxWampServer.register();
            return bladenightAuxWampServer;
        }

        private BladenightWampServerAux bladenightAuxWampServer;
    }

    private BladenightWampServerAux() {
    }


    void register() {
        getLog().debug("Registering Aux RPC handlers...");
        registerRpcHandler(BladenightUrl.GET_ACTIVE_EVENT.getText(),            new RpcHandlerGetActiveEvent(eventList));
        registerRpcHandler(BladenightUrl.GET_ALL_EVENTS.getText(),              new RpcHandlerGetAllEvents(eventList));
        registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(),            new RpcHandlerGetActiveRoute(procession));
        registerRpcHandler(BladenightUrl.GET_ROUTE.getText(),                   new RpcHandlerGetRoute(routeStore));
        registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(),         new RpcHandlerGetRealtimeUpdate(procession, Optional.empty(), false));
        registerRpcHandler(BladenightUrl.GET_ALL_ROUTE_NAMES.getText(),         new RpcHandlerGetAllRouteNames(routeStore));
        registerRpcHandler(BladenightUrl.GET_IMAGES_AND_LINKS.getText(),        new RPCHandlerGetExternalLinksAndImages(imagesAndLinkList));
    }

    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;
    private ImagesAndLinksList imagesAndLinkList;

    private static Logger log;

    public static void setLog(Logger log) {
        BladenightWampServerAux.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(BladenightWampServerAux.class.getName());
        return log;
    }
}
