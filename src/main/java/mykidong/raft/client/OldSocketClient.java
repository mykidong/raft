package mykidong.raft.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class OldSocketClient implements Client{

    private static Logger LOG = LoggerFactory.getLogger(OldSocketClient.class);

    private String host;
    private int port;
    private OutputStream out;
    private InputStream in;
    private Socket clientSocket;

    public OldSocketClient(String host, int port) {
        this.host = host;
        this.port = port;

        connect();
    }

    private void connect() {
        try {
            clientSocket = new Socket(host, port);
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            LOG.info("connected to the remote host [{}]...", clientSocket);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public ByteBuffer doRequest(ByteBuffer requestBuffer) {
        try {
            out.write(requestBuffer.array());
            out.flush();

            // Response.
            byte[] totalSizeBytes = new byte[4];
            in.read(totalSizeBytes);
            int totalSize = ByteBuffer.wrap(totalSizeBytes).getInt();

            byte[] responseMessageBytes = new byte[totalSize];
            in.read(responseMessageBytes);

            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + totalSize);
            responseBuffer.putInt(totalSize);
            responseBuffer.put(responseMessageBytes);
            responseBuffer.flip();

            return responseBuffer;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }
}
