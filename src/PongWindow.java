import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Werner
 */
public class PongWindow extends JFrame implements ActionListener {
    public static final String TITLE = "Pong - CC8";
    public static final String FONT_NAME = "Texas LED"; //"8BIT WONDER";
    private int width, height, x, y;
    protected MyCanvas canvas;
    Timer timer;
    boolean isPlay = false;
    public String RemotePlayer = null;

    private boolean isMaximized = false;
    private boolean waitForGame = false;
    private long wfgt1;
    private long wfgt2;
    private boolean searchForGame = false;

    public PongWindow() {
        super();
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.width = 800;
        this.height = 600;
        if (isMaximized) {
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            this.width = Math.max(dimension.width, dimension.height);
            this.height = this.width / 16 * 10;
            this.setUndecorated(true);
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        } else {
            this.setPreferredSize(new Dimension(width, height));
        }
        this.setSize(this.width, this.height);
        this.setTitle(TITLE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.getRootPane().setDefaultButton(null);
        this.requestFocusInWindow();
        super.setBackground(Color.BLACK);

        try {
            Image image = ImageIO.read(ClassLoader.getSystemResource("pong.gif"));
            super.setIconImage(image);
        } catch (IOException ex) {
            Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("TexasLED.TTF"));
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Add the new code before showing the window.
        canvas = new MyCanvas(this);

        this.setVisible(true);

        // Salir si presiona ESC
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == (char)27)
                    if (JOptionPane.showConfirmDialog(null, "En verdad desea salir del juego?", "Salir",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                        exit();
            }
        });

        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.setBackground(Color.BLACK);
        c.add(canvas);

        timer = new Timer(50, this);
        timer.start();
    }

    void start() {
        getContentPane().remove(canvas);
        getContentPane().add(new GameScreen(this), BorderLayout.CENTER);
    }

    void exit() {
        dispose();
        System.exit(0);
    }

    private void thisMouseClicked(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private class MyCanvas extends JPanel {

        public final PongWindow ParentWindow;

        public MyCanvas(PongWindow pongWindow) {
            super();
            this.ParentWindow = pongWindow;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    thisMouseClicked(e);
                }
            });
        }

        // draw in an overriden painting method
        @Override
        public void paintComponent(Graphics g) {
            // magic used to stop flickering when redrawing for animation later.
            super.paintComponent(g);

            setBackground(Color.BLACK);

            // Strange but we need to cast so we can paint
            Graphics2D g2 = (Graphics2D) g;

            // make drawing antialias so smooth not pixelated edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(Color.WHITE);

            // Draw stars
            Rectangle2D.Double[] r = new Rectangle2D.Double[100];
            for (int i = 0; i < r.length; i++) {
                r[i] = new Rectangle2D.Double(new Random().nextInt(width), new Random().nextInt(height), 5, 5);
                g2.draw(r[i]);
                g2.fill(r[i]);
            }

            // Draw title
            Font fontB = new Font(FONT_NAME, Font.BOLD, (int) (0.1 * height)); // scale 40/500
            Rectangle d = fontB.getStringBounds("Pong CC8", g2.getFontRenderContext()).getBounds();
            g2.setFont(fontB);
            g2.drawString("Pong CC8", width / 2 - d.width / 2, d.height);

            Font font = new Font(FONT_NAME, Font.BOLD, (int) (0.04 * height)); // scale 20/500
            g2.setFont(font);

            if (searchForGame) {
                // buscar juego en espera
                if (RemotePlayer != null) {
                    if (RemotePlayer.startsWith("ERROR")) {
                        JOptionPane.showMessageDialog(null, RemotePlayer, "Error", JOptionPane.ERROR_MESSAGE);
                        exit();
                    }
                    if (RemotePlayer.startsWith("Tiempo de espera")) {
                        searchForGame = false;
                        x = 0;
                        y = 0;
                        JOptionPane.showMessageDialog(null, RemotePlayer, "Advertencia", JOptionPane.WARNING_MESSAGE);
                        RemotePlayer = null;
                    }
                    // en este punto vuelve al menu principal si no hubo respuesta
                }

                // Draw subtitle
                Font fontC = new Font(FONT_NAME, Font.BOLD, (int) (0.08 * height)); // scale 40/500
                Rectangle ds = fontC.getStringBounds("Juego en Red", g2.getFontRenderContext()).getBounds();
                g2.setFont(fontC);
                g2.setColor(Color.lightGray);
                g2.drawString("Juego en Red", width / 2 - ds.width / 2, d.height + ds.height + g2.getFontMetrics().getAscent());
                g2.setFont(font);
                g2.setColor(Color.WHITE);

                // Show message
                String str = "Enviando solicitud" + ((RemotePlayer==null)? "" : " " + RemotePlayer) + "...";

                d = font.getStringBounds(str, g2.getFontRenderContext()).getBounds();

                // agregar puntos suspensivos animados
                wfgt2 = new Date().getTime();
                if ((wfgt2 - wfgt1) < 500)
                    str = str.substring(0, str.length() - 3);
                else if ((wfgt2 - wfgt1) < 1000)
                    str = str.substring(0, str.length() - 2);
                else if ((wfgt2 - wfgt1) < 1500)
                    str = str.substring(0, str.length() - 1);
                else wfgt1 = wfgt2;

                Rectangle2D.Double r1 = new Rectangle2D.Double(width / 2 - d.width / 2 - (int) (0.04 * height),
                        height / 2 - d.height / 2, d.width + 2 * (int) (0.04 * height), d.height + (int) (0.04 * height));
                g2.setPaint(Color.black);
                g2.fill(r1);
                g2.setColor(Color.WHITE);
                g2.draw(r1);
                g2.drawString(str, width / 2 - d.width / 2, height / 2 + g2.getFontMetrics().getAscent());

            } else if (waitForGame) {
                // un jugador ha conectado
                if (RemotePlayer != null) {
                    if (RemotePlayer.startsWith("ERROR")){
                        JOptionPane.showMessageDialog(null, RemotePlayer, "Error", JOptionPane.ERROR_MESSAGE);
                        exit();
                    }
                    start();
                }

                // Draw subtitle
                Font fontC = new Font(FONT_NAME, Font.BOLD, (int) (0.08 * height)); // scale 40/500
                Rectangle ds = fontC.getStringBounds("Juego en Red", g2.getFontRenderContext()).getBounds();
                g2.setFont(fontC);
                g2.setColor(Color.lightGray);
                g2.drawString("Juego en Red", width / 2 - ds.width / 2, d.height + ds.height + g2.getFontMetrics().getAscent());
                g2.setFont(font);
                g2.setColor(Color.WHITE);

                // Show message
                String str = "Esperando un jugador...";

                d = font.getStringBounds(str, g2.getFontRenderContext()).getBounds();

                // agregar puntos suspensivos animados
                wfgt2 = new Date().getTime();
                if ((wfgt2-wfgt1)<1000)
                    str = str.substring(0, str.length()-3);
                else if ((wfgt2-wfgt1)<2000)
                    str = str.substring(0, str.length()-2);
                else if ((wfgt2-wfgt1)<3000)
                    str = str.substring(0, str.length()-1);
                else wfgt1 = wfgt2;

                Rectangle2D.Double r1 = new Rectangle2D.Double(width / 2 - d.width / 2 - (int) (0.04 * height),
                        height / 2 - d.height / 2, d.width + 2 * (int) (0.04 * height), d.height + (int) (0.04 * height));
                g2.setPaint(Color.black);
                g2.fill(r1);
                g2.setColor(Color.WHITE);
                g2.draw(r1);
                g2.drawString(str, width / 2 - d.width / 2, height / 2 + g2.getFontMetrics().getAscent());

            } else if (!isPlay) {
                // Draw buttons
                d = font.getStringBounds("JUGAR VS PC", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r1 = new Rectangle2D.Double(width / 2 - d.width / 2 - (int) (0.04 * height),
                        height / 2 - d.height / 2, d.width + 2 * (int) (0.04 * height), d.height + (int) (0.04 * height));
                g2.setPaint(Color.black);
                g2.fill(r1);
                g2.setColor(Color.WHITE);
                g2.draw(r1);
                g2.drawString("JUGAR VS PC", width / 2 - d.width / 2, height / 2 + g2.getFontMetrics().getAscent());

                d = font.getStringBounds("JUGAR EN RED", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r2 = new Rectangle2D.Double(r1.x,
                        r1.y + d.height + g2.getFontMetrics().getAscent() * 2, r1.width, r1.height);
                g2.setPaint(Color.black);
                g2.fill(r2);
                g2.setColor(Color.WHITE);
                g2.draw(r2);
                g2.drawString("JUGAR EN RED", width / 2 - d.width / 2, (int) (r2.y + r2.height - d.height / 2));

                d = font.getStringBounds("SALIR", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r3 = new Rectangle2D.Double(r2.x,
                        r2.y + d.height + g2.getFontMetrics().getAscent() * 2, r2.width, r2.height);
                g2.setPaint(Color.black);
                g2.fill(r3);
                g2.setColor(Color.WHITE);
                g2.draw(r3);
                g2.drawString("SALIR", width / 2 - d.width / 2, (int) (r3.y + r3.height - d.height / 2));

                // Check if buttons are pressed
                if (r1.contains(new Point2D.Double(x, y))) {
                    start();
                } else if (r2.contains(new Point2D.Double(x, y))) {
                    isPlay = true;
                    x = 0;
                    y = 0;
                } else if (r3.contains(new Point2D.Double(x, y)))
                    exit();
            } else {
                // Draw subtitle
                Font fontC = new Font(FONT_NAME, Font.BOLD, (int) (0.08 * height)); // scale 40/500
                Rectangle ds = fontC.getStringBounds("Juego en Red", g2.getFontRenderContext()).getBounds();
                g2.setFont(fontC);
                g2.setColor(Color.lightGray);
                g2.drawString("Juego en Red", width / 2 - ds.width / 2, d.height + ds.height + g2.getFontMetrics().getAscent());
                g2.setFont(font);
                g2.setColor(Color.WHITE);

                // Draw buttons
                d = font.getStringBounds("INICIAR JUEGO", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r1 = new Rectangle2D.Double(width / 2 - d.width / 2 - (int) (0.04 * height),
                        height / 2 - d.height / 2, d.width + 2 * (int) (0.04 * height), d.height + (int) (0.04 * height));
                g2.setPaint(Color.black);
                g2.fill(r1);
                g2.setColor(Color.WHITE);
                g2.draw(r1);
                g2.drawString("INICIAR JUEGO", width / 2 - d.width / 2, height / 2 + g2.getFontMetrics().getAscent());

                d = font.getStringBounds("BUSCAR JUEGO", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r2 = new Rectangle2D.Double(r1.x,
                        r1.y + d.height + g2.getFontMetrics().getAscent() * 2, r1.width, r1.height);
                g2.setPaint(Color.black);
                g2.fill(r2);
                g2.setColor(Color.WHITE);
                g2.draw(r2);
                g2.drawString("BUSCAR JUEGO", width / 2 - d.width / 2, (int) (r2.y + r2.height - d.height / 2));

                d = font.getStringBounds("REGRESAR", g2.getFontRenderContext()).getBounds();
                Rectangle2D.Double r3 = new Rectangle2D.Double(r2.x,
                        r2.y + d.height + g2.getFontMetrics().getAscent() * 2, r2.width, r2.height);
                g2.setPaint(Color.black);
                g2.fill(r3);
                g2.setColor(Color.WHITE);
                g2.draw(r3);
                g2.drawString("REGRESAR", width / 2 - d.width / 2, (int) (r3.y + r3.height - d.height / 2));

                // Check if buttons are pressed
                if (r1.contains(new Point2D.Double(x, y))) {
                    // iniciar juego en red
                    waitForGame = true;
                    x = 0;
                    y = 0;
                    wfgt1 = new Date().getTime();
                    new WaitForGameThread(this.ParentWindow).start();
                } else if (r2.contains(new Point2D.Double(x, y))) {
                    // buscar juego en red
                    searchForGame = true;
                    x = 0;
                    y = 0;
                    wfgt1 = new Date().getTime();
                    try {
                        new SearchForGameThread(this.ParentWindow).start();
                    } catch (SocketException e) {
                        RemotePlayer = "ERROR: " + e.getMessage();
                    }
                } else if (r3.contains(new Point2D.Double(x, y))) {
                    isPlay = false;
                    x = 0;
                    y = 0;
                }
            }

            // release memory of graphics2d after finished drawing with it.
            g2.dispose();
        }
    }

}
