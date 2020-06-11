package mykidong.raft.util;

import mykidong.raft.db.RocksDBKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileUtils {
    private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static String createDirectoryIfNotExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                Files.createDirectories(Paths.get(path));
            }
            return path;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static void deleteDirectory(String path) {
        try {
            Files.walk(Paths.get(path))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    public static String createFileIfNotExists(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            return filePath;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}
