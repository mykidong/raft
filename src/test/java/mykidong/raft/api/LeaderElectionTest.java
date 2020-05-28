package mykidong.raft.api;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LeaderElectionTest {

    private static Logger LOG = LoggerFactory.getLogger(LeaderElectionTest.class);

    @Before
    public void init() throws Exception {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource("/log4j.xml"));
    }

    @Test
    public void leaderElectionRequest() throws Exception {
        short apiId = (short) 35;
        short version = (short) 3;
        int messageId = 1203004;
        String clientId = "any-client-id";

        Translatable<BaseRequestHeader> baseRequestHeader =
                new BaseRequestHeader(apiId, version, messageId, clientId);
        Translatable<LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader> header =
                new LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader(baseRequestHeader);

        String followerId = "any-follower-id";
        Translatable<LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody> body =
                new LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody(followerId);


        // request.
        RequestResponse<LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader,
                LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody> leaderElectionRequest = new RequestResponse<>(header, body);

        // byte buffer from request object.
        ByteBuffer buffer = leaderElectionRequest.toBuffer();
        buffer.flip();

        // new instance from the byte buffer.
        RequestResponse<LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader,
                LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody> leaderElectionRequestWithBuffer = new RequestResponse<>(buffer,
                LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader.class,
                LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody.class);

        // header from the instance constructed with byte buffer.
        LeaderElection.LeaderElectionRequest.LeaderElectionRequestHeader headerWithBuffer =
                leaderElectionRequestWithBuffer.getHeader().toObject();

        BaseRequestHeader retBaseRequestHeader = headerWithBuffer.getBaseRequestHeader();
        Assert.assertTrue(apiId == retBaseRequestHeader.getApiId());
        Assert.assertTrue(version == retBaseRequestHeader.getVersion());
        Assert.assertTrue(messageId == retBaseRequestHeader.getMessageId());
        Assert.assertTrue(clientId.equals(retBaseRequestHeader.getClientId()));

        // body from the instane constructed with byte buffer.
        LeaderElection.LeaderElectionRequest.LeaderElectionRequestBody bodyWithBuffer =
                leaderElectionRequestWithBuffer.getBody().toObject();

        Assert.assertTrue(followerId.equals(bodyWithBuffer.getFollowerId()));
    }
}
