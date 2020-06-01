package mykidong.raft.test;

import org.apache.log4j.xml.DOMConfigurator;

public abstract class TestBase {

    protected String log4jConfPath = "/log4j.xml";

    public TestBase() {
        initLog4j();
    }

    private void initLog4j() {
        // log4j init.
        DOMConfigurator.configure(this.getClass().getResource(log4jConfPath));
    }
}
