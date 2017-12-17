import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class HandlerBasic implements Runnable {

    private LinkedList <String> args = new <String> LinkedList();
    private StringBuilder hidden;
    private Socket s;
    private Clients clients;
    private String guess;
    private Map<Integer, ForkJoinTask<Integer>> pendingTasks;
    public HandlerBasic(Socket socket, Clients c){
        System.out.println("handler created");
        clients = c;
        s = socket;
        pendingTasks = null;
    }
    public HandlerBasic(Socket socket, Clients c, Map t){
        System.out.println("handler created");
        clients = c;
        s = socket;
        pendingTasks = t;
    }
    public HandlerBasic(Socket socket, Clients c, String s, Map t){
        System.out.println("handler created");
        clients = c;
        this.s = socket;
        guess = s;
        pendingTasks = t;
    }
    public void run(){
        System.out.println("computing");
        String ret;
        if(guess == null || guess.equals("new")){
            ret = getResponse("/init_");
        }else if(guess.equals("resend")){
            ret = "resend";

        }else {
            ret = getResponse("/guess_".concat(guess));
        }
        if(!ret.equals("no send")) {
            try {
                System.out.println(ret);
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                pw.println(ret);
                pw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("finished");
        }
    }
    private byte[] readFile(String filePathRelativeToRootDir) throws IOException {
        File file = new File(new File("Hangman/www"), filePathRelativeToRootDir);
        try (FileInputStream fromFile = new FileInputStream(file)) {
            byte[] buf = new byte[(int) file.length()];
            fromFile.read(buf);
            return buf;
        }catch (Exception e){
            System.out.println(e);

        }
        return null;
    }
    private String sendNotImpl(){
        return "not implemented";
    }
    private String sendString(){
        return new StringBuilder(hidden.substring(0, hidden.capacity()/3) +" Score " + Integer.toString(clients.getScore(this.s.getInetAddress().hashCode())) + "#").toString();
    }
    private boolean isCommand(String s) {
        return (s.length()>0);
    }
    private boolean isLetter(String s) {
        return (s.length()==1);
    }

    private char[] findRandomWord() throws FileNotFoundException{
        ArrayList<String> listWords = readFromFile();
        int NO_WORDS = listWords.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(NO_WORDS) + 1;
        String s = listWords.get(randIndex);
        char[] word = s.toCharArray();
        return word;
    }
    private ArrayList<String> readFromFile() throws FileNotFoundException{
        ArrayList<String> words;
        Scanner sc = new Scanner(new File("words.txt"));
        words = new ArrayList<String>();
        while(sc.hasNextLine()){
            String word = sc.nextLine();
            words.add(word);
        }
        return words;
    }
    public String getResponse(String s) {
        if (isCommand(s)) {
            String req = args.poll();
            if (s.startsWith("/init")) {
                char[] temp = null;
                if((s.length() > 6)){
                    s = s.split("_")[1];
                    temp = s.toCharArray();
                }else{
                    try {
                        temp = findRandomWord();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                hidden = new StringBuilder(temp.length*3);
                if (!clients.hasClient(s.hashCode())) {
                    for (int i = 0; i < temp.length; i++) {
                        hidden.append("_");
                    }
                    hidden.append(temp);
                    clients.addClient(this.s.getInetAddress().hashCode(), hidden);
                } else {
                    hidden = clients.getHidden(this.s.getInetAddress().hashCode());
                }
                return sendString();
            }else if (s.startsWith("/guess") && (clients.hasClient(this.s.getInetAddress().hashCode()))) {
                boolean wrong = true;
                hidden = clients.getHidden(this.s.getInetAddress().hashCode());
                s = s.split("_")[1].replace(" ", "");
                char c = s.toCharArray()[0];

                if(s.length()==1) {
                    for(int i = (hidden.capacity()*2)/3-1; i >= hidden.capacity()/3; i--){
                        if(hidden.charAt(i) == c){
                            hidden.setCharAt(i-hidden.capacity()/3, c);
                            wrong = false;
                        }
                    }
                }else{
                    if(hidden.substring(hidden.capacity()/3, hidden.capacity()*2/3).equals(s)){
                        hidden.replace(0, hidden.capacity()/3, s);
                        wrong=false;
                        c='G';
                    }else{

                    }
                }
                if(hidden.indexOf("_")==-1){
                    clients.incScore(this.s.getInetAddress().hashCode());
                    hidden.setCharAt(hidden.capacity()/3+1, '_');
                    clients.removeClient(this.s.getInetAddress().hashCode());
                    if(pendingTasks != null) {
                        pendingTasks.replace(this.s.getInetAddress().hashCode(), ForkJoinPool.commonPool().submit(new HandlerBasic(this.s, clients), 1));
                    }
                }else{
                    if (wrong){
                        if (hidden.capacity() > hidden.length()) {
                            hidden.append(c);
                        } else {
                            System.out.println("end of tries");
                            for (int i = 0; i < hidden.capacity() / 3; i++) {
                                hidden.setCharAt(i, hidden.charAt(i + hidden.capacity() / 3));

                            }
                            clients.decScore(this.s.getInetAddress().hashCode());
                            clients.removeClient(this.s.getInetAddress().hashCode());
                            if (pendingTasks != null) {
                                pendingTasks.replace(this.s.getInetAddress().hashCode(), ForkJoinPool.commonPool().submit(new HandlerBasic(this.s, clients), 1));
                            }
                        }
                    }
                }
                return sendString();
            }
        }else{
            String req = args.poll();
            if(req != null){
                return sendNotImpl();
            }
        }
        return ("not supported");
    }
}
