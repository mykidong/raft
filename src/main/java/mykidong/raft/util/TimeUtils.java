package mykidong.raft.util;

public class TimeUtils {

    public static void pause(long pauseInMilliseconds) {
        try {
            Thread.sleep(pauseInMilliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
