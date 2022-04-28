package sk.spedry.weebbotcollector.util.lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMServer;

import java.util.ArrayList;
import java.util.List;

public class ServerList {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<WCMServer> list;

    public ServerList() {
        this.list = new ArrayList<WCMServer>();
    }

    public List<WCMServer> getServerList() {
        return list;
    }

    public void addServer(WCMServer server) {
        this.list.add(server);
    }

    public int getSize() {
        return this.list.size();
    }
}
