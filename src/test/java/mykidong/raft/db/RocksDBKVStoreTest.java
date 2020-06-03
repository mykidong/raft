package mykidong.raft.db;

import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class RocksDBKVStoreTest extends TestBase {

    @Test
    public void run() throws Exception {
        String dbPath = "/tmp/raft/rocksdb";
        if(!new File(dbPath).exists()) {
            Files.createDirectories(Paths.get(dbPath));
        }
        RocksDBKVStore rocksDBKVStore = RocksDBKVStore.open(dbPath);

        String key = "any-key";

        String name = "any-name";
        int age = 40;
        String address = "any-address";
        User user = new User(name, age, address);

        rocksDBKVStore.save(key, user);

        Optional<User> optionalUser = rocksDBKVStore.find(key, User.class);
        Assert.assertTrue(optionalUser.isPresent());

        User retUser = optionalUser.get();
        Assert.assertTrue(name.equals(retUser.getName()));
        Assert.assertTrue(age == retUser.getAge());
        Assert.assertTrue(address.equals(retUser.getAddress()));

        boolean saved = rocksDBKVStore.save(key, user);
        Assert.assertTrue(saved);

        boolean deleted = rocksDBKVStore.delete(key);
        Assert.assertTrue(deleted);

        Optional<User> retOptional = rocksDBKVStore.find(key, User.class);
        Assert.assertTrue(!retOptional.isPresent());
    }

    private static class User {
        private String name;
        private int age;
        private String address;

        // NOTE: no-arg constructor must exist to be used by kryo serializer.
        public User() {
        }

        public User(String name, int age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getAddress() {
            return address;
        }
    }
}
