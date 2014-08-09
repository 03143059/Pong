import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * Created by Werner on 7/24/2014.
 */
public class Marquee extends JFrame implements ActionListener {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Marquee();
            }
        });
    }

    MyScroller scroller;

    public Marquee() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(350, 200));
        setSize(new Dimension(350, 200));
        getContentPane().setBackground(Color.black);
        scroller =   new MyScroller();
        getContentPane().add(scroller);
        pack();
        setVisible(true);
        new Timer(1000, this).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scroller.repaint();
        scroller.append("Testing: " + new Date() + "\n");
    }
}

