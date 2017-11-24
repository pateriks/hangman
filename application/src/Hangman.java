import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
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
    }
    public void displayMessage(String s){
        textArea.append(s + "\n");

    }
}
