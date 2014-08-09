import javax.swing.*;
import java.awt.*;

/**
 * Created by Werner on 7/22/2014.
 */
public class MyScroller extends JPanel {

    JTextArea textArea;
    Font font;
    String temp = System.lineSeparator();

    public MyScroller() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("terminal.TTF"));
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        textArea = new JTextArea();
        textArea.setFont(new Font("Terminal", Font.PLAIN, 12));
        textArea.setForeground(Color.gray);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        setBackground(Color.black);
        JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(null);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        add(pane, BorderLayout.CENTER);
    }

    public void append(String text) {
        if (textArea.getText().length() == 0){
            int numLines = textArea.getHeight() / textArea.getFontMetrics(textArea.getFont()).getHeight();
            for (int i = 1; i < numLines; i++)
                textArea.append(System.lineSeparator());
        }
        int tl = textArea.getText().length();
        textArea.select(tl-temp.length(), tl);
        textArea.replaceSelection(text);
        textArea.append(text);
        textArea.setCaretPosition(textArea.getText().length());
        temp = text;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, w, h);
        super.paint(g);
        h /= 4;
        Color color2 = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        Color color1 = new Color(0.0f, 0.0f, 0.0f, 1.0f);
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        color2 = new Color(0.0f, 0.0f, 0.0f, 1.0f);
        color1 = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        h = g2d.getFontMetrics().getHeight();
        gp = new GradientPaint(0, getHeight()-h*2, color1, 0, getHeight()-h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0,  getHeight()-h*2, w,  h*2);
    }

}
