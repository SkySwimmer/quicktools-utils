package usr.skyswimmer.quicktoolsutils.io;

import java.io.File;
import java.nio.file.Files;

public class FileUtils {

	public static void deleteDir(File dir) {
		if (Files.isSymbolicLink(dir.toPath())) {
			// Skip symlink
			dir.delete();
			return;
		}
		if (!dir.exists())
			return;
		for (File subDir : dir.listFiles(t -> t.isDirectory())) {
			deleteDir(subDir);
		}
		for (File file : dir.listFiles(t -> !t.isDirectory())) {
			file.delete();
		}
		dir.delete();
	}

}
