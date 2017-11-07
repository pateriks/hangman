import java.io.*;
import java.net.Socket;

public class Handler implements Runnable {
    private StringBuilder sb;
    private BufferedReader br;
    private PrintWriter pw;
    private Socket clientSocket;

    public Handler(Socket s){
        this.clientSocket = s;

    }
    public void run(){
        try{
            boolean autoFlush = true;
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            pw = new PrintWriter(clientSocket.getOutputStream(), autoFlush);
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }
}
