package mykidong.raft.api;

import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class HeartbeatTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(HeartbeatTest.class);

    @Test
    public void heartbeatRequest() throws Exception {

        // base request header.
        short apiId = (short) 35;
        short version = (short) 3;
        int messageId = 1203004;
        String clientId = "any-client-id";
        Translatable<BaseRequestHeader> baseRequestHeader = new BaseRequestHeader(apiId, version, messageId, clientId);

        // request header.
        Translatable<HeartbeatRequest.HeartbeatRequestHeader> requestHeader = new HeartbeatRequest.HeartbeatRequestHeader(baseRequestHeader);

        // request boday.
        String leaderId = "any-leader-id";
        Translatable<HeartbeatRequest.HeartbeatRequestBody> requestBody = new HeartbeatRequest.HeartbeatRequestBody(leaderId);

        // request.
        RequestResponse<HeartbeatRequest.HeartbeatRequestHeader,
                HeartbeatRequest.HeartbeatRequestBody> request = new RequestResponse<>(requestHeader, requestBody);

        // byte buffer from request object.
        ByteBuffer buffer = request.toBuffer();
        buffer.flip();

        // new instance from the byte buffer.
        RequestResponse<HeartbeatRequest.HeartbeatRequestHeader,
                HeartbeatRequest.HeartbeatRequestBody> requestWithBuffer =
                new RequestResponse<>(buffer, HeartbeatRequest.HeartbeatRequestHeader.class,
                                              HeartbeatRequest.HeartbeatRequestBody.class);

        HeartbeatRequest.HeartbeatRequestBody bodyWithBuffer = requestWithBuffer.getBody().toObject();
        Assert.assertTrue(leaderId.equals(bodyWithBuffer.getLeaderId()));
    }

    @Test
    public void heartbeatResponse() throws Exception {
        // base response header.
        int correlationId = 1203004;
        Translatable<BaseResponseHeader> baseResponseHeader = new BaseResponseHeader(correlationId);

        // response header.
        Translatable<HeartbeatResponse.HeartbeatResponseHeader> responseHeader =
                new HeartbeatResponse.HeartbeatResponseHeader(baseResponseHeader);

        // base response body.
        short errorCode = 0;
        Translatable<BaseResponseBody> baseResponseBody = new BaseResponseBody(errorCode);

        // response body.
        String followerId = "any-follower-id";
        Translatable<HeartbeatResponse.HeartbeatResponseBody> responseBody =
                new HeartbeatResponse.HeartbeatResponseBody(baseResponseBody, followerId);

        // response.
        RequestResponse<HeartbeatResponse.HeartbeatResponseHeader,
                HeartbeatResponse.HeartbeatResponseBody> response = new RequestResponse<>(responseHeader, responseBody);

        // buffer returned from response instance.
        ByteBuffer buffer = response.toBuffer();
        buffer.flip();


        // new response instance created with buffer.
        RequestResponse<HeartbeatResponse.HeartbeatResponseHeader,
                HeartbeatResponse.HeartbeatResponseBody> responseWithBuffer =
                new RequestResponse<>(buffer, HeartbeatResponse.HeartbeatResponseHeader.class,
                                            HeartbeatResponse.HeartbeatResponseBody.class);

        HeartbeatResponse.HeartbeatResponseBody bodyWithBuffer = responseWithBuffer.getBody().toObject();
        BaseResponseBody baseResponseBodyWithBuffer = bodyWithBuffer.getBaseHeaderBody().toObject();
        Assert.assertTrue(errorCode == baseResponseBodyWithBuffer.getErrorCode());

        // if errorCode is 0, all response body fields can be retrieved.
        if(errorCode == 0) {
            Assert.assertTrue(followerId.equals(bodyWithBuffer.getFollowerId()));
            LOG.info("followerId: [{}]", bodyWithBuffer.getFollowerId());
        }
        // if errorCode is not 0,
        // all the other fields in response body except errorCode have not been set in response.
        else {
            Assert.assertNull(bodyWithBuffer.getFollowerId());
        }
    }
}
