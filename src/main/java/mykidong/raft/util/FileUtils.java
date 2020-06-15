package mykidong.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

    public static ByteBuffer getMMap(String filePath, long position, long length) {
        ByteBuffer buffer = null;
        FileChannel fileChannel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            fileChannel = raf.getChannel();
            buffer = getMMap(fileChannel, position, length);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            try {
                fileChannel.close();
                LOG.debug("file channel [{}] closed...", filePath);
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }

        return buffer;
    }

    public static ByteBuffer getMMap(FileChannel fileChannel, long position, long length) {
        try {
            return fileChannel.map(FileChannel.MapMode.READ_WRITE, position, length).duplicate();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}
