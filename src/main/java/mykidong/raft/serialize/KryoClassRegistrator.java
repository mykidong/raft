package mykidong.raft.serialize;

import com.esotericsoftware.kryo.Kryo;

public interface KryoClassRegistrator {
	
	public void register(Kryo kryo);

}
