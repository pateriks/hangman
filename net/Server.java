import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private ServerSocket server_sock;
    private Socket client;
    private Thread thread;

    protected void createSocketServer(Clients clients){
        thread = new Thread(() -> {
            try {
                server_sock = new ServerSocket(8888);
                while (true) {
                    Socket s = server_sock.accept();
                    s.setSoLinger(true, 5000);
                    s.setSoTimeout(1800000);//Half an hour
                    Thread handler = new Thread(new Handler(s, clients));
                    handler.setPriority(Thread.MAX_PRIORITY);
                    handler.start();
                }
            } catch (IOException e) {
                //System.out.println(e);
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
    public static void main (String[] args) {
        Server test = new Server();
        test.createSocketServer(new Clients());
        Scanner scanner = new Scanner (System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("quit")){
                System.out.println("Not_implemented: stop by entering ^C");
            } else if(input.equals("hej")){
                System.out.println("Hejhej");
            }
            input="";
        }
    }
}
