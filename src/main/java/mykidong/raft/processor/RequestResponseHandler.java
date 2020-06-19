package mykidong.raft.processor;

import mykidong.raft.api.*;
import mykidong.raft.config.Configurator;
import mykidong.raft.controller.Controllable;
import mykidong.raft.controller.LeaderHeartbeatTimerTask;
import mykidong.raft.controller.VoteTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.TimerTask;

public class RequestResponseHandler implements Handlerable {

    private static Logger LOG = LoggerFactory.getLogger(RequestResponseHandler.class);

    // TODO: handle leader election.
    private Controllable controllable;
    private TimerTask voteTimerTask;
    private TimerTask leaderHeartbeatTimerTask;
    private TimerTask followerHeartbeatTimerTask;

    private Configurator configurator;

    public RequestResponseHandler(Controllable controllable, Configurator configurator) {
        this.controllable = controllable;
        this.configurator = configurator;

        // TODO: add concrete implementations of timer tasks.
        leaderHeartbeatTimerTask = new LeaderHeartbeatTimerTask(controllable, configurator);
        voteTimerTask = new VoteTimerTask(controllable, leaderHeartbeatTimerTask, configurator);

        // vote timer task must be set before leader election controller thread get started.
        this.controllable.setVoteTimerTask(voteTimerTask);
    }

    @Override
    public Attachment handleRequest(BaseRequestHeader baseRequestHeader, ByteBuffer requestBuffer) {
        Attachment attachment = null;
        short apiId = baseRequestHeader.getApiId();
        short version = baseRequestHeader.getVersion();

        int correlationId = baseRequestHeader.getMessageId();
        Translatable<BaseResponseHeader> baseResponseHeader = new BaseResponseHeader(correlationId);

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
        } else if(apiId == HeartbeatRequest.API_ID) {

            // ========================== request ====================================
            RequestResponse<HeartbeatRequest.HeartbeatRequestHeader,
                    HeartbeatRequest.HeartbeatRequestBody> request =
                    new RequestResponse<>(requestBuffer, HeartbeatRequest.HeartbeatRequestHeader.class,
                            HeartbeatRequest.HeartbeatRequestBody.class);

            HeartbeatRequest.HeartbeatRequestBody requestBody = request.getBody().toObject();
            String leaderId = requestBody.getLeaderId();

            // TODO: handle request.

            // ========================== response ====================================
            // response header.
            Translatable<HeartbeatResponse.HeartbeatResponseHeader> responseHeader =
                    new HeartbeatResponse.HeartbeatResponseHeader(baseResponseHeader);

            // base response body.
            short errorCode = 0;
            Translatable<BaseResponseBody> baseResponseBody = new BaseResponseBody(errorCode);

            // response body.
            // TODO: set follower id.
            String followerId = "any-follower-id";
            Translatable<HeartbeatResponse.HeartbeatResponseBody> responseBody =
                    new HeartbeatResponse.HeartbeatResponseBody(baseResponseBody, followerId);

            // response.
            RequestResponse<HeartbeatResponse.HeartbeatResponseHeader,
                    HeartbeatResponse.HeartbeatResponseBody> response = new RequestResponse<>(responseHeader, responseBody);

            // response buffer.
            ByteBuffer responseBuffer = response.toBuffer();
            responseBuffer.rewind();

            attachment = new Attachment(baseRequestHeader, null, responseBuffer);
        }
        // TODO: handle the requests of other apis.

        return attachment;
    }

    @Override
    public ByteBuffer handleResponse(Attachment attachment) {
        BaseRequestHeader baseRequestHeader = attachment.getBaseRequestHeader();
        short apiId = baseRequestHeader.getApiId();
        short version = baseRequestHeader.getVersion();
        ByteBuffer responseBuffer = null;
        if(apiId == LeaderElectionRequest.API_ID ||
                apiId == HeartbeatRequest.API_ID) {
            responseBuffer = attachment.getResponseBuffer();
        }
        // TODO: handle the responses of other apis.

        return responseBuffer;
    }
}
