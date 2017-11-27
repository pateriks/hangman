import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class Handler implements Runnable {

    private LinkedList <String> args = new <String> LinkedList();
    private StringBuilder hidden;
    private SocketChannel sC;
    private Clients clients;
    private String guess;

    public Handler(SocketChannel k, Clients c){
        System.out.println("handler created");
        clients = c;
        sC = k;
    }
    public Handler(SocketChannel k, Clients c, String s){
        System.out.println("handler created");
        clients = c;
        sC = k;
        guess = s;
    }
    public void run(){
        System.out.println("computing");
        String ret;
        if(guess == null){
            ret = getResponse("/init_");
        }else if(guess.equals("resend")){
            ret = "resend";

        }else {
            ret = getResponse("/guess_".concat(guess));
        }
        ByteBuffer buffer = ByteBuffer.wrap(ret.getBytes());
        try {
            sC.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("finished");
    }
    private byte[] readFile(String filePathRelativeToRootDir) throws IOException {
        File file = new File(new File("../../www"), filePathRelativeToRootDir);
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
        return new StringBuilder(hidden.substring(0, hidden.capacity()/3) +" Score " + Integer.toString(clients.getScore(sC.hashCode()))).toString();
    }
    private boolean isCommand(String s) {
        return (s.length()>0);
    }
    private boolean isLetter(String s) {
        return (s.length()==1);
    }
    private String getMimeFromExtension(String name) {
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".txt") || name.endsWith(".java")) {
            return "text/plain";
        } else if (name.endsWith(".gif")) {
            return "image/gif";
        } else if (name.endsWith(".class")) {
            return "application/octet-stream";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (name.endsWith(".css")) {
            return "stylecheet/css";
        }else {
            return "text/plain";
        }
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
                System.out.println("init");
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
                if (!clients.hasClient(sC.hashCode())) {
                    for (int i = 0; i < temp.length; i++) {
                        hidden.append("_");
                    }
                    hidden.append(temp);
                    clients.addClient(sC.hashCode(), hidden);
                } else {
                    hidden = clients.getHidden(sC.hashCode());
                }
                return sendString();
            }else if (s.startsWith("/guess") && (clients.hasClient(sC.hashCode()))) {
                boolean wrong = true;
                hidden = clients.getHidden(sC.hashCode());
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
                    clients.incScore(sC.hashCode());
                    hidden.setCharAt(hidden.capacity()/3+1, '_');
                    clients.removeClient(sC.hashCode());
                }else{
                    if (wrong){
                        if (hidden.capacity() > hidden.length()) {
                            hidden.append(c);
                        } else {
                            System.out.println("end of tries");
                            for(int i=0; i<hidden.capacity()/3; i++){
                                hidden.setCharAt(i, hidden.charAt(i+hidden.capacity()/3));

                            }
                            clients.decScore(sC.hashCode());
                            clients.removeClient(sC.hashCode());
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
