package mykidong.raft;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaftServer {

    private static Logger log = LoggerFactory.getLogger(RaftServer.class);

    private int port = 9912;

    public RaftServer(int port) {
        this.port = port;

        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));

        // socket channel queue.
        BlockingQueue<SocketChannel> socketChannelQueue = new LinkedBlockingQueue<>();
        long socketChannelQueuePollTimeout = 1000;

        // request queue.
        BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();
        long requestQueuePollTimeout = 1000;

        // channel handlers.
        List<ChannelHandler> channelHandlers = new ArrayList<>();
        for(int i = 0; i < 5; i++)
        {
            channelHandlers.add(new ChannelHandler(socketChannelQueue, requestQueue, socketChannelQueuePollTimeout));
        }

        // start channel handlers.
        for(ChannelHandler channelHandler : channelHandlers) {
            channelHandler.start();
        }

        // request processors.
        List<RequestProcessor> requestProcessors = new ArrayList<>();
        for(int i = 0; i < 5; i++)
        {
            requestProcessors.add(new RequestProcessor(requestQueue, requestQueuePollTimeout));
        }

        // start request processors.
        for(RequestProcessor requestProcessor : requestProcessors) {
            requestProcessor.start();
        }

        // socket server.
        SocketServer socketServer = new SocketServer(this.port, socketChannelQueue);

        // start socket server.
        socketServer.start();

        log.info("socket server is listening on " + port + " ...");
    }
}
