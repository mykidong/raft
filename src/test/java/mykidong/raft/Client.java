package mykidong.raft;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client {

    private static Logger log = LoggerFactory.getLogger(Client.class);

    @Test
    public void sendSimpleMessage() throws Exception {

        // start raft server.
        int port = 9912;
        RaftServer raftServer = new RaftServer(port);

        Thread.sleep(1000000000);


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

            log.info("request sent..." + i);


            // Response.
            byte[] totalSizeBytes = new byte[4];
            in.read(totalSizeBytes);
            int totalSize = ByteBuffer.wrap(totalSizeBytes).getInt();

            byte[] responseMessageBytes = new byte[totalSize];
            in.read(responseMessageBytes);

            log.info("response: " + new String(responseMessageBytes));
            log.info("===========================================================");

            buffer.clear();
            Thread.sleep(1000);
        }

    }
}
