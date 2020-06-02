package mykidong.raft.processor;

import mykidong.raft.api.Attachment;
import mykidong.raft.api.BaseRequestHeader;

import java.nio.ByteBuffer;

public interface Handlerable {

    Attachment handleRequest(BaseRequestHeader baseRequestHeader, ByteBuffer buffer);

    ByteBuffer handleResponse(Attachment attachment);
}
