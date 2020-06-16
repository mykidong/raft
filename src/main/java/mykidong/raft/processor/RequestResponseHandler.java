package mykidong.raft.processor;

import mykidong.raft.api.*;
import mykidong.raft.controller.Controllable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class RequestResponseHandler implements Handlerable {

    private static Logger LOG = LoggerFactory.getLogger(RequestResponseHandler.class);

    // TODO: handle leader election.
    private Controllable controllable;

    public RequestResponseHandler(Controllable controllable) {
        this.controllable = controllable;
    }

    @Override
    public Attachment handleRequest(BaseRequestHeader baseRequestHeader, ByteBuffer requestBuffer) {
        Attachment attachment = null;
        short apiId = baseRequestHeader.getApiId();
        short version = baseRequestHeader.getVersion();

        if(apiId == LeaderElectionRequest.API_ID) {
            RequestResponse<LeaderElectionRequest.LeaderElectionRequestHeader,
                    LeaderElectionRequest.LeaderElectionRequestBody> request = new RequestResponse<>(requestBuffer,
                    LeaderElectionRequest.LeaderElectionRequestHeader.class,
                    LeaderElectionRequest.LeaderElectionRequestBody.class);

            // ========================== request ====================================
            LeaderElectionRequest.LeaderElectionRequestHeader requestHeader =
                    request.getHeader().toObject();

            LeaderElectionRequest.LeaderElectionRequestBody requestBody = request.getBody().toObject();
            String candidateId = requestBody.getCandidateId();
            int newTermNumber = requestBody.getNewTermNumber();
            int lastTermNumber = requestBody.getLastTermNumber();
            long lastLogIndexNumber = requestBody.getLastLogIndexNumber();

            // TODO: handle request.
            LOG.debug("api id: [{}]", apiId);


            // ========================== response ====================================
            int correlationId = baseRequestHeader.getMessageId();
            Translatable<BaseResponseHeader> baseResponseHeader = new BaseResponseHeader(correlationId);

            Translatable<LeaderElectionResponse.LeaderElectionResponseHeader> responseHeader =
                    new LeaderElectionResponse.LeaderElectionResponseHeader(baseResponseHeader);

            short errorCode = 0;
            Translatable<BaseResponseBody> baseResponseBody = new BaseResponseBody(errorCode);

            // TODO: set follower id.
            String followerId = "any-follower-id";
            Translatable<LeaderElectionResponse.LeaderElectionResponseBody> responseBody =
                    new LeaderElectionResponse.LeaderElectionResponseBody(baseResponseBody, followerId);

            RequestResponse<LeaderElectionResponse.LeaderElectionResponseHeader,
                    LeaderElectionResponse.LeaderElectionResponseBody> response = new RequestResponse<>(responseHeader, responseBody);

            ByteBuffer responseBuffer = response.toBuffer();
            responseBuffer.rewind();

            attachment = new Attachment(baseRequestHeader, null, responseBuffer);
        }

        return attachment;
    }

    @Override
    public ByteBuffer handleResponse(Attachment attachment) {
        BaseRequestHeader baseRequestHeader = attachment.getBaseRequestHeader();
        short apiId = baseRequestHeader.getApiId();
        short version = baseRequestHeader.getVersion();
        if(apiId == LeaderElectionRequest.API_ID) {
            ByteBuffer responseBuffer = attachment.getResponseBuffer();
            return responseBuffer;
        }

        return null;
    }
}
