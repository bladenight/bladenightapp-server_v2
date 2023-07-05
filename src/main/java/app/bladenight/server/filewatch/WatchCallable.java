package app.bladenight.server.filewatch;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class WatchCallable implements Callable<Void> {
    private final Path path;
    private final FileWatcherCallback fileWatcherCallback;

    public  WatchCallable(Path path,FileWatcherCallback callback) {
        this.path = path;
        this.fileWatcherCallback=callback;
    }

    @Override
    public Void call() throws Exception {

        WatchFolder wf = new WatchFolder();
        wf.watchFolder(path,fileWatcherCallback);

        return null;
    }

}