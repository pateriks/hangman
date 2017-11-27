import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private Thread thread;
    private ServerSocketChannel ssC;
    private Selector selector;
    private boolean run;
    public LinkedList <String> que = new LinkedList<>();
    private HashMap<Integer, ForkJoinTask<Integer>> lookup = new HashMap();

    private void initSelector() throws IOException {
        selector = Selector.open();
    }
    private void initSSC() throws IOException {
        ssC = ServerSocketChannel.open();
        ssC.configureBlocking(false);
        ssC.bind(new InetSocketAddress(8888));
        ssC.register(selector, SelectionKey.OP_ACCEPT);
    }
    public void send(SelectionKey key, String s, Clients clients) throws IOException {
        ForkJoinTask<Integer> task = ForkJoinPool.commonPool().submit(new Handler((SocketChannel)key.channel(), clients, s), 1);
        lookup.put(key.channel().hashCode(), task);
    }
    private String receive(SelectionKey key) throws IOException {
        SocketChannel sC = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        sC.read(buffer);
        buffer.flip();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(buffer);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return charBuffer.toString();
    }
    private void start(SelectionKey key, Clients clients) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel sC = serverSocketChannel.accept();
        sC.configureBlocking(false);
        sC.register(selector, SelectionKey.OP_READ);
        ForkJoinTask<Integer> task = ForkJoinPool.commonPool().submit(new Handler(sC, clients), 1);
        lookup.put(sC.hashCode(), task);

        System.out.println("started");
    }
    protected void createSocketServer(Clients clients) throws IOException{
        run = true;
        new Thread(() -> {
            while (run) {
                try {
                    if (selector.select() == 0) {
                        System.out.println("no channels ready");
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext() && run) {
                        System.out.println("iterate");
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            System.out.println("accepted");
                            start(key, clients);
                        } else if (key.isReadable()) {
                            String getMsg = receive(key);

                            ForkJoinTask<Integer> task = lookup.get(key.channel().hashCode());
                            if(getMsg.equals("bye")){
                                run = false;
                                task.cancel(true);
                                key.channel().close();

                                break;
                            }
                            else if (task.get() == 1) {
                                send(key, getMsg, clients);
                            }else{
                                send(key, "resend", clients);
                            }
                        }
                    }
                }catch(Exception e) {
                    System.out.println(e);
                }
            }
            try {
                ssC.close();
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void main (String[] args) {

        Server test = new Server();
        try {
            test.initSelector();
            test.initSSC();
            test.createSocketServer(new Clients());
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
