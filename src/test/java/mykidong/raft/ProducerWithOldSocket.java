package mykidong.raft;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProducerWithOldSocket {

    private static Logger LOG = LoggerFactory.getLogger(ProducerWithOldSocket.class);

    @Test
    public void runMultipleClients() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<String>> futureList = new ArrayList<Future<String>>();


        for(int x = 0; x < 10; x++) {
            Future<String> future = executor.submit(() -> {
                return this.sendSimpleMessage();
            });

            futureList.add(future);
        }

        for (Future<String> fut : futureList) {
            try {
                System.out.println(new Date() + "::" + fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }

    @Test
    public void sendMessagesWithSingleClient() throws Exception {
        sendSimpleMessage();
    }


    private String sendSimpleMessage() throws Exception {
        int port = 9912;
        Socket clientSocket = new Socket("localhost", port);

        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();


        for(int i = 0; i < 5; i++) {
            byte[] messageBytes = new String("this is client request..." + i).getBytes();
            int messageLength = messageBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(4 + messageLength);
            buffer.putInt(messageBytes.length);
            buffer.put(messageBytes);

            buffer.rewind();

            byte[] requestBytes = new byte[4 + messageLength];
            buffer.get(requestBytes);

            out.write(requestBytes);
            out.flush();

            LOG.info("request sent..." + i);


            // Response.
            byte[] totalSizeBytes = new byte[4];
            in.read(totalSizeBytes);
            int totalSize = ByteBuffer.wrap(totalSizeBytes).getInt();

            byte[] responseMessageBytes = new byte[totalSize];
            in.read(responseMessageBytes);

            LOG.info("response: " + new String(responseMessageBytes));
            LOG.info("===========================================================");

            buffer.clear();
            //Thread.sleep(1000);
        }

        return "OK";
    }
}
