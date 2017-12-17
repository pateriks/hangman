import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Hangman extends JFrame {
    private Client c;
    private JTextField userText;
    private JTextArea  textArea;
    public Hangman (Client c, LinkedList<String> que){
        this.c = c;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        que.push(e.getActionCommand());

                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        textArea = new JTextArea();
        add(new JScrollPane(textArea));
        setSize(300, 150);
        setVisible(true);
        userText.setEditable(true);
        new Thread(()->{
            int i = 0;
            int print = 97;
            Random r = new Random();
            while(i<0){
                print = 97 + r.nextInt(25);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                que.push(Character.toString(Character.toChars(print)[0]));

                i++;
            }
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(que.toString());

            }

        }
        ).start();
    }
    public void displayMessage(String s){
        textArea.append(s + "\n");

    }
}
