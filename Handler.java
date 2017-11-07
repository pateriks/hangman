import java.io.*;
import java.net.Socket;
import java.util.*;

import java.util.Date;
public class Handler implements Runnable {
    private LinkedList <String> args = new <String> LinkedList();
    private BufferedReader br;
    private PrintWriter pw;
    private Socket clientSocket;
    private OutputStream outStream;

    private static final boolean AUTOFLUSH = true;
    private static final String SERVER_ID_HEADER = "Server: Httpd 1.0";
    private static final String HTTP_GET_METHOD = "GET";
    private static final String HTTP_OK_RESPONSE = "HTTP/1.0 200 OK";
    private static final String NOT_FOUND_RESPONSE = "HTTP/1.0 404 File Not Found";
    private static final String NOT_FOUND_HTML = "<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD><BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>";
    private static final String HTTP_NOT_IMPL_RESPONSE = "HTTP/1.0 501 Not Implemented";
    private static final String NOT_IMPL_HTML = "<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD><BODY><H1>HTTP Error 501: Not Implemented";

    public Handler(Socket s){
        this.clientSocket = s;
    }
    public void run(){
        try{
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = clientSocket.getOutputStream();
            pw = new PrintWriter(outStream, false);
            if(br.ready()){
              String line = br.readLine();

              String[] temp = line.split(" ");
              for(String ins : temp){
                              System.out.println(ins + " Hello " + args.size());
                args.push(ins);
              }
            }
            System.out.println("Hello " + args.size());
            if(args.poll().startsWith("HTTP/")){
              if(args.poll().equals("/")){
                if(args.poll().equals(HTTP_GET_METHOD)){
                  byte[] fileContent = readFile("index.html");
                  pw.println(HTTP_OK_RESPONSE);
                  System.out.println("heS");
                  pw.println("Date:" + java.time.LocalDate.now());
                  pw.println(SERVER_ID_HEADER);
                  pw.println("Content-length: " + fileContent.length);
                  pw.println("Content-type: " + getMimeFromExtension("index.html"));
                  pw.println();
                  pw.flush();
                  outStream.write(fileContent);
                }
              }else{
                String req = args.poll();
                if(req != null){
                  pw.println(NOT_FOUND_RESPONSE);
                  pw.println("Date:" + (new Date()));
                  pw.println(SERVER_ID_HEADER);
                  pw.println("Content-type: text/html");
                  pw.println();
                  pw.print(HTTP_NOT_IMPL_RESPONSE);
                  pw.flush();
                }
              }
            }
        }catch(IOException e){
            System.out.println(e);
        }finally{
          try{
            clientSocket.close();
          }catch(IOException e){
          }
        }
    }

    private byte[] readFile(String filePathRelativeToRootDir) throws IOException {
        File file = new File(filePathRelativeToRootDir);
        try (FileInputStream fromFile = new FileInputStream(file)) {
            byte[] buf = new byte[(int) file.length()];
            fromFile.read(buf);
            return buf;
        }
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
        } else {
            return "text/plain";
        }
    }
}
