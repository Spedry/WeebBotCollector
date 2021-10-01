package sk.spedry.weebbotcollector.util.lists;

import sk.spedry.weebbotcollector.util.WCMServer;

import java.util.ArrayList;
import java.util.List;

public class ServerList {
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
