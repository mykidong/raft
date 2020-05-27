package mykidong.raft;

import mykidong.raft.client.SocketClient;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunClient {

    private static Logger LOG = LoggerFactory.getLogger(RunClient.class);

    @Before
    public void init() throws Exception {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));
    }

    @Test
    public void runMultipleClients() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<String>> futureList = new ArrayList<Future<String>>();

        long start = System.currentTimeMillis();

        int TASK_MAX = 20;
        int messageCount = 100000;
        for(int i = 0; i < TASK_MAX; i++) {
            Future<String> future = executor.submit(() -> {
                return sendSimpleMessages(messageCount);
            });
            futureList.add(future);
        }

        for (Future<String> fut : futureList) {
            try {
                String result = new Date() + "::" + fut.get();
                LOG.info(result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        LOG.info("tps all: [{}]", ((double)(TASK_MAX * messageCount) / (double)(System.currentTimeMillis() - start)) * 1000);

        executor.shutdown();
    }

    @Test
    public void runSingleClient() throws Exception {
        String tps = sendSimpleMessages(1000);
        LOG.info("tps: [{}]", tps);
    }

    private static String sendSimpleMessages(int count) {

        String host = "localhost";
        int port = 9912;
        SocketClient client = new SocketClient(host, port);
        client.start();

        long start = System.currentTimeMillis();
        int MAX = count;
        for(int i = 0; i < MAX; i++) {
            ByteBuffer buffer = buildRequest(i);

            ByteBuffer responseBuffer = client.sendMessage(buffer);
            printResponse(responseBuffer);
        }
        String tps = "tps: " + ((double)MAX / (double)(System.currentTimeMillis() - start)) * 1000;

        return tps;
    }

    private static ByteBuffer buildRequest(int i) {
        byte[] messageBytes = new String("this is client request..." + i).getBytes();
        int messageLength = messageBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageLength);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();

        return buffer;
    }

    private static void printResponse(ByteBuffer buffer) {
        buffer.rewind();
        LOG.debug("response: [{}]", new String(buffer.array()));
    }
}
