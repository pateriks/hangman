import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerBasic {
    private Thread thread;
    private ServerSocket ss;
    private Selector selector;
    private boolean run;
    public LinkedList <String> que = new LinkedList<>();
    private Map <Integer, ForkJoinTask<Integer>> lookup = Collections.synchronizedMap(new HashMap<Integer, ForkJoinTask<Integer>>());

    private void initSS() throws IOException {
        ss = new ServerSocket((8888));
    }
    public void send(Socket socket, String s, Clients clients) throws IOException {
        ForkJoinTask<Integer> task = ForkJoinPool.commonPool().submit(new HandlerBasic(socket, clients, s, lookup), 1);
        if(!s.equals("resend")) {
            lookup.put(s.hashCode(), task);
        }
    }
    private String receive(Socket s) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return br.readLine();
    }
    private Socket start(Clients clients) throws IOException {
        Socket s = ss.accept();
        ForkJoinTask<Integer> task = ForkJoinPool.commonPool().submit(new HandlerBasic(s, clients), 1);
        lookup.put(s.getInetAddress().hashCode(), task);
        System.out.println("started");
        return s;
    }
    protected void createSocketServer(Clients clients, List<Socket> activeClients) throws IOException{
        run = true;
        new Thread(() -> {
            while (run) {
                try {
                    System.out.println("iterate");
                    Socket client = start(clients);
                    new Thread(()->{
                        while(true) {
                            try {
                                String getMsg = receive(client);
                                System.out.println(getMsg);
                                ForkJoinTask<Integer> task = lookup.get(client.getInetAddress().hashCode());
                                if (getMsg.equals("bye")) {
                                    client.close();
                                    break;
                                } else if (task.isDone()) {
                                    send(client, getMsg, clients);
                                } else {
                                    send(client, "resend", clients);
                                }
                                System.out.println(getMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }).start();
                    activeClients.add(client);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            for(Socket s : activeClients){
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static void main (String[] args) {

        ServerBasic test = new ServerBasic();
        try {
            test.initSS();
            test.createSocketServer(new Clients(), new LinkedList<Socket>());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner (System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("quit")){
                test.run = false;
                System.out.println("Not_implemented: stop by entering ^C");
            } else if(input.equals("hej")){
                System.out.println("Hejhej");
            }
            input="";
        }
    }

}
