package mykidong.raft.api;

import mykidong.raft.test.TestBase;
import mykidong.raft.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class BufferUtilsTest extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(BufferUtils.class);

    @Test
    public void getHeader() throws Exception {
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
        buffer.rewind();


        Translatable<BaseRequestHeader> newBaeRequestHeader = new BaseRequestHeader(buffer);
        BaseRequestHeader baseRequestHeaderObj = newBaeRequestHeader.toObject();
        Assert.assertTrue(apiId == baseRequestHeaderObj.getApiId());
        Assert.assertTrue(version == baseRequestHeaderObj.getVersion());
        Assert.assertTrue(messageId == baseRequestHeaderObj.getMessageId());
        Assert.assertTrue(clientId.equals(baseRequestHeaderObj.getClientId()));

        buffer.rewind();

        // new instance from the byte buffer.
        RequestResponse<LeaderElectionRequest.LeaderElectionRequestHeader,
                LeaderElectionRequest.LeaderElectionRequestBody> leaderElectionRequestWithBuffer = new RequestResponse<>(buffer,
                LeaderElectionRequest.LeaderElectionRequestHeader.class,
                LeaderElectionRequest.LeaderElectionRequestBody.class);

        // body from the instance constructed with byte buffer.
        LeaderElectionRequest.LeaderElectionRequestBody bodyWithBuffer =
                leaderElectionRequestWithBuffer.getBody().toObject();

        Assert.assertTrue(candidateId.equals(bodyWithBuffer.getCandidateId()));
        Assert.assertTrue(newTermNumber == bodyWithBuffer.getNewTermNumber());
        Assert.assertTrue(lastTermNumber == bodyWithBuffer.getLastTermNumber());
        Assert.assertTrue(lastLogIndexNumber == bodyWithBuffer.getLastLogIndexNumber());
    }

    @Test
    public void convert() throws Exception {
        String str = "hello, this is raft ...";
        byte[] bytes = StringUtils.getBytes(str);
        int size = bytes.length;

        ByteBuffer requestResponseBuffer = ByteBuffer.allocate(size);
        requestResponseBuffer.put(bytes);

        ByteBuffer messageBuffer = BufferUtils.toMessageBuffer(requestResponseBuffer);

        byte[] retBytes = new byte[size];
        ByteBuffer retRequestResponseBuffer = BufferUtils.toRequestResponseBuffer(messageBuffer);
        retRequestResponseBuffer.get(retBytes);

        LOG.debug("ret str: [{}]", StringUtils.toString(retBytes));
        Assert.assertTrue(str.equals(StringUtils.toString(retBytes)));
    }
}
