import java.io.*;
import java.net.*;

public class Server {

    private static int port_number;
    private PrintWriter out;
    private BufferedReader in;
    private ServerSocket server_sock;
    private Socket client;

    protected void createSocketServer(){
        boolean open = true;
        port_number = 8888;
        while(open) {
            try {
                server_sock = new ServerSocket(port_number);
                Socket s = server_sock.accept();
                open = false;
                s.setSoLinger(true, 5000);
                Thread handler = new Thread(new Handler(s));
                handler.setPriority(Thread.MAX_PRIORITY);
                handler.start();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
    public static void main (String[] args){
        Server test = new Server();
        test.createSocketServer();
    }
}
