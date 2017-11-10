import java.net.InetAddress;
import java.util.LinkedHashMap;


public class Clients {
    LinkedHashMap<Integer, StringBuilder> clients = new LinkedHashMap<>();
    public Clients(){

    }
    public void addClient(InetAddress address, StringBuilder hidden) {
        if (clients.containsKey(address.hashCode())) {
            clients.replace(address.hashCode(), hidden);
        } else {
            clients.put(address.hashCode(), hidden);
        }
    }
    public void removeClient(InetAddress address){
        clients.remove(address.hashCode());
    }
    public boolean hasClient(InetAddress address){
        return clients.containsKey(address.hashCode());
    }
    public StringBuilder getHidden(InetAddress address){
        return clients.get(address.hashCode());
    }
}