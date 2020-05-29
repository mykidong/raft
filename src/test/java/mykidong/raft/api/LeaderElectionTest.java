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
        // base request header.
        short apiId = (short) 35;
        short version = (short) 3;
        int messageId = 1203004;
        String clientId = "any-client-id";

        Translatable<BaseRequestHeader> baseRequestHeader =
                new BaseRequestHeader(apiId, version, messageId, clientId);
        Translatable<LeaderElectionRequest.LeaderElectionRequestHeader> header =
                new LeaderElectionRequest.LeaderElectionRequestHeader(baseRequestHeader);

        // request body.
        String candidateId = "any-candidate-id";
        int newTermNumber = 350;
        int lastTermNumber = 349;
        long lastLogIndexNumber = 12030033L;
        Translatable<LeaderElectionRequest.LeaderElectionRequestBody> body =
                new LeaderElectionRequest.LeaderElectionRequestBody(candidateId, newTermNumber, lastTermNumber, lastLogIndexNumber);

        // request.
        RequestResponse<LeaderElectionRequest.LeaderElectionRequestHeader,
                LeaderElectionRequest.LeaderElectionRequestBody> leaderElectionRequest = new RequestResponse<>(header, body);

        // byte buffer from request object.
        ByteBuffer buffer = leaderElectionRequest.toBuffer();
        buffer.flip();

        // new instance from the byte buffer.
        RequestResponse<LeaderElectionRequest.LeaderElectionRequestHeader,
                LeaderElectionRequest.LeaderElectionRequestBody> leaderElectionRequestWithBuffer = new RequestResponse<>(buffer,
                LeaderElectionRequest.LeaderElectionRequestHeader.class,
                LeaderElectionRequest.LeaderElectionRequestBody.class);

        // header from the instance constructed with byte buffer.
        LeaderElectionRequest.LeaderElectionRequestHeader headerWithBuffer =
                leaderElectionRequestWithBuffer.getHeader().toObject();

        BaseRequestHeader retBaseRequestHeader = headerWithBuffer.getBaseRequestHeader();
        Assert.assertTrue(apiId == retBaseRequestHeader.getApiId());
        Assert.assertTrue(version == retBaseRequestHeader.getVersion());
        Assert.assertTrue(messageId == retBaseRequestHeader.getMessageId());
        Assert.assertTrue(clientId.equals(retBaseRequestHeader.getClientId()));

        // body from the instance constructed with byte buffer.
        LeaderElectionRequest.LeaderElectionRequestBody bodyWithBuffer =
                leaderElectionRequestWithBuffer.getBody().toObject();

        Assert.assertTrue(candidateId.equals(bodyWithBuffer.getCandidateId()));
        Assert.assertTrue(newTermNumber == bodyWithBuffer.getNewTermNumber());
        Assert.assertTrue(lastTermNumber == bodyWithBuffer.getLastTermNumber());
        Assert.assertTrue(lastLogIndexNumber == bodyWithBuffer.getLastLogIndexNumber());
    }

    @Test
    public void leaderElectionResponse() throws Exception {
        // base response header.
        int correlationId = 223;

        Translatable<BaseResponseHeader> baseResponseHeader = new BaseResponseHeader(correlationId);

        // header.
        Translatable<LeaderElectionResponse.LeaderElectionResponseHeader> header =
                new LeaderElectionResponse.LeaderElectionResponseHeader(baseResponseHeader);

        // base response body.
        short errorCode = 1;
        Translatable<BaseResponseBody> baseResponseBody = new BaseResponseBody(errorCode);

        // response body.
        String followerId = "any-follower-id";
        Translatable<LeaderElectionResponse.LeaderElectionResponseBody> body =
                new LeaderElectionResponse.LeaderElectionResponseBody(baseResponseBody, followerId);

        // response.
        RequestResponse<LeaderElectionResponse.LeaderElectionResponseHeader,
                LeaderElectionResponse.LeaderElectionResponseBody> leaderElectionResponse = new RequestResponse<>(header, body);

        // buffer returned from response instance.
        ByteBuffer buffer = leaderElectionResponse.toBuffer();
        buffer.flip();


        // new response instance created with buffer.
        RequestResponse<LeaderElectionResponse.LeaderElectionResponseHeader,
                LeaderElectionResponse.LeaderElectionResponseBody> leaderElectionResponseWithBuffer =
                new RequestResponse<>(buffer, LeaderElectionResponse.LeaderElectionResponseHeader.class,
                        LeaderElectionResponse.LeaderElectionResponseBody.class);

        // response header.
        LeaderElectionResponse.LeaderElectionResponseHeader headerWithBuffer = leaderElectionResponseWithBuffer.getHeader().toObject();
        Assert.assertTrue(correlationId == headerWithBuffer.getBaseResponseHeader().toObject().getCorrelationId());

        LeaderElectionResponse.LeaderElectionResponseBody bodyWithBuffer = leaderElectionResponseWithBuffer.getBody().toObject();
        BaseResponseBody baseResponseBodyWithBuffer = bodyWithBuffer.getBaseResponseBody().toObject();
        Assert.assertTrue(errorCode == baseResponseBodyWithBuffer.getErrorCode());

        if(errorCode == 0) {
            LOG.info("followerId: [{}]", bodyWithBuffer.getFollowerId());
            Assert.assertTrue(followerId.equals(bodyWithBuffer.getFollowerId()));
        }
        else {
            Assert.assertNull(bodyWithBuffer.getFollowerId());
        }
    }
}
