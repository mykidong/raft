package mykidong.raft.config;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfiguratorTest extends TestBase {

    @Test
    public void configure() throws Exception {

        String confStr = "/config/raft.yml";
        String log4jConfStr = "/log/log4j.xml";
        int portInt = 9913;
        String nodesStr = "0:localhost:9912,1:localhost:9913,2:localhost:9914";

        List<String> argsList = new ArrayList<>();
        argsList.add("--" + Configuration.CONF.getArgConf());
        argsList.add(confStr);
        argsList.add("--" + Configuration.LOG4J_CONF.getArgConf());
        argsList.add(log4jConfStr);
        argsList.add("--" + Configuration.SERVER_PORT.getArgConf());
        argsList.add("" + portInt);
        argsList.add("--" + Configuration.NODE_LIST.getArgConf());
        argsList.add(nodesStr);

        String[] args = argsList.toArray(new String[0]);

        OptionParser parser = new OptionParser();
        parser.accepts(Configuration.CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.LOG4J_CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.SERVER_PORT.getArgConf()).withRequiredArg().ofType(Integer.class);
        parser.accepts(Configuration.NODE_LIST.getArgConf()).withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String conf = (options.has(Configuration.CONF.getArgConf())) ? (String) options.valueOf(Configuration.CONF.getArgConf())
                : (String) Configuration.CONF.getDefaultValue();

        // load configuration.
        Configurator configurator = YamlConfigurator.open(conf);

        // log4j conf.
        Optional optionalLog4JConf =  configurator.get(Configuration.LOG4J_CONF.getConf());
        String log4jConf = (options.has(Configuration.LOG4J_CONF.getArgConf())) ? (String) options.valueOf(Configuration.LOG4J_CONF.getArgConf())
                : ((optionalLog4JConf.isPresent()) ? (String) optionalLog4JConf.get() : (String) Configuration.LOG4J_CONF.getDefaultValue());

        // update log4j conf to configurator.
        configurator.put(Configuration.LOG4J_CONF.getConf(), log4jConf);
        Assert.assertEquals(log4jConfStr, (String) configurator.get(Configuration.LOG4J_CONF.getConf()).get());

        // server port.
        Optional optionalServerPortConf =  configurator.get(Configuration.SERVER_PORT.getConf());
        int serverPort = (options.has(Configuration.SERVER_PORT.getArgConf())) ? (Integer) options.valueOf(Configuration.SERVER_PORT.getArgConf())
                : ((optionalServerPortConf.isPresent()) ? (Integer) optionalServerPortConf.get() : (Integer) Configuration.SERVER_PORT.getDefaultValue());

        // update server port conf to configurator.
        configurator.put(Configuration.SERVER_PORT.getConf(), serverPort);
        Assert.assertTrue(portInt == (Integer) configurator.get(Configuration.SERVER_PORT.getConf()).get());

        // node list.
        List<String> nodeList = new ArrayList<>();
        if(options.has(Configuration.NODE_LIST.getArgConf())) {
            String nodes = (String) options.valueOf(Configuration.NODE_LIST.getArgConf());
            for(String nodeLine : nodes.split(",")) {
                nodeList.add(nodeLine);
            }
        } else {
            Optional optionalNodeListConf =  configurator.get(Configuration.NODE_LIST.getConf());
            nodeList = (optionalNodeListConf.isPresent()) ? (List<String>) optionalNodeListConf.get()
                    : (List<String>) Configuration.NODE_LIST.getDefaultValue();
        }

        // update node list conf to configurator.
        configurator.put(Configuration.NODE_LIST.getConf(), nodeList);
        Assert.assertEquals(3, ((List<String>)configurator.get(Configuration.NODE_LIST.getConf()).get()).size());
    }

    @Test
    public void argsOption() throws Exception {

        String confStr = "/config/raft.yml";
        String log4jConfStr = "/log/log4j.xml";

        List<String> argsList = new ArrayList<>();
        argsList.add("--" + Configuration.CONF.getArgConf());
        argsList.add(confStr);
        argsList.add("--" + Configuration.LOG4J_CONF.getArgConf());
        argsList.add(log4jConfStr);

        String[] args = argsList.toArray(new String[0]);

        OptionParser parser = new OptionParser();
        parser.accepts(Configuration.CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.LOG4J_CONF.getArgConf()).withRequiredArg().ofType(String.class);
        parser.accepts(Configuration.SERVER_PORT.getArgConf()).withRequiredArg().ofType(Integer.class);

        OptionSet options = parser.parse(args);

        String conf = (options.has(Configuration.CONF.getArgConf())) ? (String) options.valueOf(Configuration.CONF.getArgConf())
                : (String) Configuration.CONF.getDefaultValue();

        // load configuration.
        Configurator configurator = YamlConfigurator.open(conf);

        // log4j conf.
        Optional optionalLog4JConf =  configurator.get(Configuration.LOG4J_CONF.getConf());
        String log4jConf = (options.has(Configuration.LOG4J_CONF.getArgConf())) ? (String) options.valueOf(Configuration.LOG4J_CONF.getArgConf())
                : ((optionalLog4JConf.isPresent()) ? (String) optionalLog4JConf.get() : (String) Configuration.LOG4J_CONF.getDefaultValue());

        // update log4j conf to configurator.
        configurator.put(Configuration.LOG4J_CONF.getConf(), log4jConf);
        Assert.assertEquals(log4jConfStr, (String) configurator.get(Configuration.LOG4J_CONF.getConf()).get());
    }
}
