package app.bladenight.server.filewatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDirectoryExecutor {
    private static final Logger logger = LogManager.getLogger(WatchDirectoryExecutor.class);
    public ExecutorService startFolderWatcher(Path path, FileWatcherCallback fileWatcherCallback) {
        ExecutorService executor = Executors.newCachedThreadPool();

        executor.submit(new WatchCallable(path,fileWatcherCallback));
        logger.info("started watching for changes at that path:" +path);
        return executor;

    }
}