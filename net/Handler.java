import java.io.*;
import java.net.Socket;
import java.util.*;

public class Handler implements Runnable {

    private LinkedList <String> args = new <String> LinkedList();
    private StringBuilder sb = new StringBuilder();
    private BufferedReader br;
    private PrintWriter pw;
    private Socket clientSocket;
    private OutputStream outStream;
    private Clients clients;
    private StringBuilder hidden;

    private static final boolean AUTOFLUSH = true;
    private static final String SERVER_ID_HEADER = "Server: Httpd 1.0";
    private static final String HTTP_GET_METHOD = "GET";
    private static final String HTTP_OK_RESPONSE = "HTTP/1.0 200 OK";
    private static final String NOT_FOUND_RESPONSE = "HTTP/1.0 404 File Not Found";
    private static final String NOT_FOUND_HTML = "<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD><BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>";
    private static final String HTTP_NOT_IMPL_RESPONSE = "HTTP/1.0 501 Not Implemented";
    private static final String NOT_IMPL_HTML = "<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD><BODY><H1>HTTP Error 501: Not Implemented";
    private static final String hangmanHead = "<HTML><HEAD><TITLE>HANGMAN</TITLE></HEAD><BODY>";
    private static final String hangmanTail = "</BODY></HTML>";

    public Handler(Socket s, Clients c){
        this.clients = c;
        this.clientSocket = s;
    }

    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = clientSocket.getOutputStream();
            pw = new PrintWriter(outStream, false);
            String line = br.readLine();
            String[] tempo = line.split(" ");
            for(String ins : tempo){
                System.out.println(ins);
                args.push(ins);
            }

            while ((line = br.readLine()) != null && !line.trim().equals("")) {
                sb.append(line);
            }
            if(args.size() < 2){
                System.out.println("ERR: FREDRIK");
            }else if(args.poll().startsWith("HTTP/")){
              String s = args.poll();
              if(s.equals("/")){
                if(args.poll().equals(HTTP_GET_METHOD)){
                  byte[] fileContent = readFile("/index.html");
                  pw.println(HTTP_OK_RESPONSE);
                  pw.println("Date:" + java.time.LocalDate.now());
                  pw.println(SERVER_ID_HEADER);
                  pw.println("Content-length: " + fileContent.length);
                  pw.println("Content-type: " + getMimeFromExtension("/index.html"));
                  pw.println();
                  pw.flush();
                  outStream.write(fileContent);
                }else{
                    notImplemented();
                }
              } else if(s.equals("/hangman.html")){
                  String req = args.poll();
                  if(req.equals(HTTP_GET_METHOD)) {
                      pw.println(HTTP_OK_RESPONSE);
                      pw.println("Date:" + java.time.LocalDate.now());
                      pw.println(SERVER_ID_HEADER);
                      pw.println("Content-type: " + getMimeFromExtension("hangman.html"));
                      pw.println();

                      pw.print(hangmanHead + "Guesses = " + hangmanTail);
                      pw.flush();
                      clients.decScore(clientSocket.getInetAddress());
                      clients.removeClient(clientSocket.getInetAddress());
                  }else{
                      notImplemented();
                  }
              } else if(s.endsWith(".css")){
                  String req = args.poll();
                  if(req.equals(HTTP_GET_METHOD)) {
                      byte[] fileContent = readFile(s);
                      pw.println(HTTP_OK_RESPONSE);
                      pw.println("Date:" + java.time.LocalDate.now());
                      pw.println(SERVER_ID_HEADER);
                      pw.println("Content-length: " + fileContent.length);
                      pw.println("Content-type: " + getMimeFromExtension("index.html"));
                      pw.println();
                      pw.flush();
                      outStream.write(fileContent);
                  }else{
                      notImplemented();
                  }
              } else if (isCommand(s)) {
                  String req = args.poll();
                  if (s.startsWith("/init")) {
                      char[] temp;
                      if((s.length() > 6)){
                          s = s.split("_")[1];
                          temp = s.toCharArray();
                      }else{
                          temp = findRandomWord();
                      }
                      hidden = new StringBuilder(temp.length*3);
                      if (!clients.hasClient(clientSocket.getInetAddress())) {
                          for (int i = 0; i < temp.length; i++) {
                              hidden.append("_");
                          }
                          hidden.append(temp);
                          clients.addClient(clientSocket.getInetAddress(), hidden);
                      } else {
                          hidden = clients.getHidden(clientSocket.getInetAddress());
                      }
                      if (req.equals(HTTP_GET_METHOD)) {
                          pw.println(HTTP_OK_RESPONSE);
                          pw.println("Date:" + java.time.LocalDate.now());
                          pw.println(SERVER_ID_HEADER);
                          pw.println("Content-type: " + getMimeFromExtension("hangman.html"));
                          pw.println();
                          pw.print(hangmanHead + hidden.substring(0, hidden.capacity()/3) + hangmanTail);
                          pw.print(" Score " + Integer.toString(clients.getScore(clientSocket.getInetAddress())));
                          pw.flush();
                      }else{
                          notImplemented();
                      }
                  }else if (s.startsWith("/guess") && (clients.hasClient(clientSocket.getInetAddress()))) {
                      boolean wrong = true;
                      hidden = clients.getHidden(clientSocket.getInetAddress());
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
                          clients.incScore(clientSocket.getInetAddress());
                          hidden.setCharAt(hidden.capacity()/3+1, '_');
                          clients.removeClient(clientSocket.getInetAddress());
                      }else{
                          if (wrong){
                              if (hidden.capacity() > hidden.length()) {
                                  hidden.append(c);
                              } else {
                                  System.out.println("end of tries");
                                  for(int i=0; i<hidden.capacity()/3; i++){
                                      hidden.setCharAt(i, hidden.charAt(i+hidden.capacity()/3));

                                  }
                                  clients.decScore(clientSocket.getInetAddress());
                                  clients.removeClient(clientSocket.getInetAddress());
                              }
                          }
                      }
                      if (req.equals(HTTP_GET_METHOD)) {
                          pw.println(HTTP_OK_RESPONSE);
                          pw.println("Date:" + java.time.LocalDate.now());
                          pw.println(SERVER_ID_HEADER);
                          pw.println("Content-type: " + getMimeFromExtension("hangman.html"));
                          pw.println();
                          pw.print(hangmanHead + hidden.substring(0, hidden.capacity()/3) + " Score " + clients.getScore(clientSocket.getInetAddress()) + hangmanTail);
                          pw.flush();
                      }else{
                        notImplemented();
                      }
                  }
              }else{
                String req = args.poll();
                if(req != null){
                  notImplemented();
                }
              }
            }
        }catch(IOException e){
            //System.out.println(e);
        }finally{
          try{
              clientSocket.close();
          }catch(IOException e){
          }
        }
    }
    private void notImplemented(){
        pw.println(NOT_FOUND_RESPONSE);
        pw.println("Date:" + (new Date()));
        pw.println(SERVER_ID_HEADER);
        pw.println("Content-type: text/html");
        pw.println();
        pw.print(HTTP_NOT_IMPL_RESPONSE);
        pw.flush();
    }
    private byte[] readFile(String filePathRelativeToRootDir) throws IOException {
        File file = new File(new File("../www"), filePathRelativeToRootDir);
        try (FileInputStream fromFile = new FileInputStream(file)) {
            byte[] buf = new byte[(int) file.length()];
            fromFile.read(buf);
            return buf;
        }catch (Exception e){
            System.out.println(e);

        }
        return null;
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
}
