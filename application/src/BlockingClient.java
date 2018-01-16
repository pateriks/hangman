import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class BlockingClient {

    String input = "";
    String display = "";
    BufferedReader br;
    PrintWriter pw;
    boolean open = true;
    LinkedList<String> prev = new LinkedList<>();
    Socket s;

    public BlockingClient(String host, int port){
        try {
            s = new Socket(InetAddress.getByName(host), port);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            pw = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        prev.push("");
    }

    public static void main (String[] args){
        BlockingClient bc = new BlockingClient("localhost", 8888);
        bc.startConnection();
        bc.open();

        while (true){
            bc.getUserInput();
            bc.sendResponse();
        }

    }

    private void getResponse(){
        try {
            display = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(){
        if(!prev.get(0).equals(input)) {
            prev.add(input);
            if (pw != null)
                out("Guessing: "+ Arrays.toString(prev.subList(1, prev.size()).toArray()));
                pw.println(input);
                pw.flush();
        }
    }

    private void getUserInput(){
        Scanner in = new Scanner(System.in);
        this.input = in.nextLine();
    }

    private void startConnection() {
        new Thread(() -> {
            while (open) {
                String string = display;
                out(string);
                if(display == null){
                    open = false;
                    break;
                }
                while (string.equals(display)) {
                    getResponse();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void close(){
        this.open = false;
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open(){
        this.open = true;
    }

    private void out(String s){
        System.out.println(s);
    }

}
