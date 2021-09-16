package org.deepfake_http.common.dir_watcher;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryWatcher implements Runnable {

	private Logger   logger;
	private Path     dirPath;
	private Runnable runnable;

	private Collection<Path> filePaths = new HashSet<>();

	public DirectoryWatcher(Logger logger, Path dirPath, Runnable runnable) {
		this.logger   = logger;
		this.dirPath  = dirPath;
		this.runnable = runnable;
	}

	public void addFile(Path filePath) {
		filePaths.add(filePath);
	}

	private void fileChangedEventHandler(WatchEvent<?> event) {
		Kind<?> kind = event.kind();
		Path    path = (Path) event.context();

		for (Path filePath : filePaths) {
			if (filePath.equals(path)) {
				logger.log(Level.INFO, "File \"{0}\" was changed. Kind: {1}.", new Object[] { dirPath.resolve(filePath), kind });
				try {
					runnable.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			WatchService watchService = dirPath.getFileSystem().newWatchService();
			dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

			while (true) {
				WatchKey watchKey;
				watchKey = watchService.take();

				for (final WatchEvent<?> event : watchKey.pollEvents())
					fileChangedEventHandler(event);

				if (!watchKey.reset()) {
					watchKey.cancel();
					watchService.close();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
