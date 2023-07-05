package app.bladenight.server.filewatch;

import app.bladenight.server.App;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WatchFileChange extends FileWatchdog {
    private static final Logger logger = LogManager.getLogger(WatchFileChange.class);
    public WatchFileChange(String filename) {
        super(filename);
    }

    @Override
    protected void doOnChange() {
        //is calles on init
        logger.info(filename+"changed - update routeStore");
        App.initializeApplicationConfiguration();
    }

}