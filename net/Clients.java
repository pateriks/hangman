import java.net.InetAddress;
import java.util.LinkedHashMap;


public class Clients {
    LinkedHashMap<Integer, StringBuilder> clients = new LinkedHashMap<>();
    LinkedHashMap<Integer, Integer> score = new LinkedHashMap<>();
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
    public void clearClients(InetAddress address){
        clients.clear();
        score.clear();
    }
    public boolean hasClient(InetAddress address){
        return clients.containsKey(address.hashCode());
    }
    public StringBuilder getHidden(InetAddress address){
        return clients.get(address.hashCode());
    }
    public Integer getScore(InetAddress address){
        if (score.containsKey(address.hashCode())) {
            return score.get(address.hashCode());
        } else {
            return 0;
        }
    }
    public void incScore(InetAddress address){
        if (score.containsKey(address.hashCode())) {
            score.replace(address.hashCode(), score.get(address.hashCode())+1);
        } else {
            score.put(address.hashCode(), 1);
        }
    }
    public void decScore(InetAddress address){
        if (score.containsKey(address.hashCode())) {
            score.replace(address.hashCode(), score.get(address.hashCode())-1);
        } else {
            score.put(address.hashCode(), -1);
        }
    }
}