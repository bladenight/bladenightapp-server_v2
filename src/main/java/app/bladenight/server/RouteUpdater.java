package app.bladenight.server;

import app.bladenight.common.events.Event;
import app.bladenight.common.events.EventList;
import app.bladenight.common.network.messages.EventMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.routes.Route;
import app.bladenight.common.routes.RouteStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouteUpdater implements Runnable {
    private long period;
    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;
    private Route lastRoute;
    private static Logger log;

    public RouteUpdater(Procession procession, RouteStore routeStore, EventList eventList, long period) {
        this.procession = procession;
        this.routeStore = routeStore;
        this.eventList = eventList;
        this.period = period;
    }

    @Override
    public void run() {
        boolean cont = true;
        while (cont) {
            update();
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
                cont = false;
            }
        }
    }

    void update() {
        Route currentRoute = procession.getRoute();

        Event nextEvent = eventList.getNextEvent();
        if (nextEvent != null) {
            if (currentRoute == null || !currentRoute.getName().equals(nextEvent.getRouteName())) {
                Route route = routeStore.getRoute(nextEvent.getRouteName());
                if (route == null) {
                    getLog().error("Route for next event unknown: " + nextEvent.getRouteName());
                    procession.setRoute(null);
                    procession.setEventStatus(EventMessage.convertStatus(nextEvent.getStatus()));
                } else {
                    getLog().info("Active route changed to " + route.getName());
                    procession.setRoute(route);
                    procession.setEventStatus(EventMessage.convertStatus(nextEvent.getStatus()));
                }
            }
        } else {
            getLog().warn("No upcoming event found");
            procession.setEventStatus(EventMessage.EventStatus.NOE);
            procession.setRoute(null);
        }

    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RouteUpdater.class.getName());
        return log;
    }

}

