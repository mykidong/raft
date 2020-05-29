package mykidong.raft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    private static Logger LOG = LoggerFactory.getLogger(StringUtils.class);

    public static byte[] getBytes(String str) {
        return getBytes(str, "UTF-8");
    }

    public static byte[] getBytes(String str, String charset) {
        try {
            return str.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static String toString(byte[] bytes) {
        return toString(bytes, "UTF-8");
    }

    public static String toString(byte[] bytes, String charset) {
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}
