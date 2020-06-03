package mykidong.raft.db;

import mykidong.raft.serialize.DefaultKryoContext;
import mykidong.raft.serialize.KryoContext;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// TODO: rockdb transaction should be supported ???

public class RocksDBKVStore<V> implements KVStore<String, V> {
    private static Logger LOG = LoggerFactory.getLogger(RocksDBKVStore.class);

    private KryoContext kryoContext = DefaultKryoContext.newKryoContextFactory(kryo -> {
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
    });

    private String dbPath;
    private RocksDB db;
    private static final Object lock = new Object();
    private static Map<String, RocksDBKVStore> rocksdbMap;

    public static RocksDBKVStore open(String dbPath)
    {
        if(rocksdbMap == null) {
            synchronized(lock) {
                if(rocksdbMap == null) {

                    rocksdbMap = new ConcurrentHashMap<>();

                    RocksDBKVStore rocksDBKVStore = new RocksDBKVStore(dbPath);

                    rocksdbMap.put(dbPath, rocksDBKVStore);
                    LOG.info("map is initialized, dbPath: [" + dbPath + "]");
                }
            }
        }
        else
        {
            synchronized(lock) {
                if (!rocksdbMap.containsKey(dbPath)) {
                    RocksDBKVStore rocksDBKVStore = new RocksDBKVStore(dbPath);
                    rocksdbMap.put(dbPath, rocksDBKVStore);
                    LOG.info("dbPath: [" + dbPath + "] not exists, new rocksdb put to map.");
                }
            }
        }

        return rocksdbMap.get(dbPath);
    }


    private RocksDBKVStore(String dbPath) {
        this.dbPath = dbPath;

        initDB();
    }

    private void initDB() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            db = RocksDB.open(options, dbPath);
        } catch (RocksDBException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public boolean save(String key, V value) {
        byte[] valueBytes = kryoContext.serialze(value);
        try {
            db.put(key.getBytes(), valueBytes);
        } catch (RocksDBException e) {
            LOG.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public Optional<V> find(String key, Class<V> valueClass) {
        try {
            byte[] valueBytes = db.get(key.getBytes());
            if (valueBytes != null) {
                V value = (V) kryoContext.deserialze(valueClass, valueBytes);
                return Optional.of(value);
            } else {
                return Optional.empty();
            }
        } catch (RocksDBException e) {
            LOG.error(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            db.delete(key.getBytes());
        } catch (RocksDBException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }
}
