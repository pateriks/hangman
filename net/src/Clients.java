import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;


public class Clients {
    LinkedHashMap<Integer, StringBuilder> clients = new LinkedHashMap<>();
    LinkedHashMap<Integer, Integer> score = new LinkedHashMap<>();
    public Clients(){

    }
    public void addClient(int key, StringBuilder hidden) {
        if (clients.containsKey(key)) {
            clients.replace(key, hidden);
        } else {
            clients.put(key, hidden);
        }
    }
    public void removeClient(int key){
        clients.remove(key);
    }
    public void clearClients(){
        clients.clear();
        score.clear();
    }
    public Collection getClients(){
        return clients.values();

    }
    public boolean hasClient(int key){
        return clients.containsKey(key);
    }
    public StringBuilder getHidden(int key){
        return clients.get(key);
    }
    public Integer getScore(int key){
        if (score.containsKey(key)) {
            return score.get(key);
        } else {
            return 0;
        }
    }
    public void incScore(int key){
        if (score.containsKey(key)) {
            score.replace(key, score.get(key)+1);
        } else {
            score.put(key, 1);
        }
    }
    public void decScore(int key){
        if (score.containsKey(key)) {
            score.replace(key, score.get(key)-1);
        } else {
            score.put(key, -1);
        }
    }
}