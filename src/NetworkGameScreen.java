import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;


public class NetworkGameScreen extends JPanel implements ActionListener {

    public static final double HORIZONTAL_SCALE = 0.0125;
    public static final double VERTICAL_SCALE = 0.12;
    public static final double BALL_SCALE = 0.025;
    public static final String FONT_NAME = "Texas LED";// "8BIT WONDER";
    public static final double BALL_SPEED = 0.005;
    public static final int SCORE_SHOW_DELAY = 10;
    public static final double PADDLE_SPEED = 0.01167;

    private Timer aiTimer, playerTimer;
    private int movement, points, _points, scoreY;
    private int width, height;
    private Ball ball;
    private final Paddle player;
    private final RemotePaddle ai;
    private boolean right, down, ballMovement;
    private boolean[] keys;
    public boolean[] remoteKeys;
    private final URL url, _url;
    private final AudioClip clipContact, clipScore;
    JFrame parent;
    private boolean isMouseDown;
    private int mouseY;

    public LinkedBlockingQueue<String> keystr =  new LinkedBlockingQueue<String>();

    public enum NetworkGameType {
        MASTER,
        SLAVE
    }

    private NetworkGameType networkGameType;

    public NetworkGameScreen(JFrame parent, NetworkGameType networkGameType) {
        this.networkGameType = networkGameType;
        this.parent = parent;
        this.width = parent.getContentPane().getWidth();
        this.height = parent.getContentPane().getHeight();
        super.setSize(width, height);
        this.width -= 10;
        super.setBackground(Color.BLACK);
        keys = new boolean[256];
        for (int i = 0; i < keys.length; i++)
            keys[i] = false;
        remoteKeys = new boolean[256];
        for (int i = 0; i < remoteKeys.length; i++)
            remoteKeys[i] = false;
        parent.addKeyListener(new MyKeyListener());
        parent.addMouseWheelListener(new MyMouseWheelListener());
        parent.addMouseListener(new MyMouseListener());
        parent.addMouseMotionListener(new MyMouseMotionListener());
        setFocusable(true);
        points = 0;
        _points = 0;
        movement = 10;
        scoreY = height/5;
        ball = getBall();
        ball.setSpeed((int) (height * BALL_SPEED)); // 6
        player = getPaddle();
        player.setSpeed((int)(height * PADDLE_SPEED)); // 7
        ai = getRemotePaddle();
        ai.setSpeed((int)(height * PADDLE_SPEED));
        url = Pong.class.getResource("pongLimit.wav");
        _url = Pong.class.getResource("pongScore.wav");
        clipContact = Applet.newAudioClip(url);
        clipScore = Applet.newAudioClip(_url);

        // ai and ball Timer
        aiTimer = new Timer(10, this);
        aiTimer.start();

        ballMovement = true;

    }

    private RemotePaddle getRemotePaddle() {
        if (networkGameType == NetworkGameType.MASTER)
            return new RemotePaddle(width-(int)(HORIZONTAL_SCALE * width), 0, width, height, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
        else
            return new RemotePaddle(0, 0, width, height, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
    }

    private Paddle getPaddle() {
        if (networkGameType == NetworkGameType.MASTER)
            return new Paddle(0, 0, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
        else
            return new Paddle(width-(int)(HORIZONTAL_SCALE * width), 0, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
    }

    private Ball getBall() {
        if (networkGameType == NetworkGameType.MASTER)
            return new Ball(100, 40, (int)(BALL_SCALE * width), (int)(BALL_SCALE * width));
        else
            return new Ball(width - 110, 40, (int)(BALL_SCALE * width), (int)(BALL_SCALE * width));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        try {
            g2.draw(ball);
            g2.fill(ball);
            g2.draw(player);
            g2.fill(player);
            g2.draw(ai);
            g2.fill(ai);
            Font f = new Font(FONT_NAME, Font.BOLD, height/5);
            g2.setFont(f);        //OLD WILD 8 Bit FONT :D
            int wp = (int)f.getStringBounds(""+_points, g2.getFontRenderContext()).getWidth();
            g2.drawString("" + _points, width / 2 + 30, scoreY+30);
            int wh = (int)f.getStringBounds(""+points, g2.getFontRenderContext()).getWidth();
            g2.drawString("" + points, width / 2 - wh-30, scoreY+30);
            for (int i = 0; i < height + 10; i += 10) {
                Rectangle2D.Double r = new Rectangle2D.Double(width / 2, i, 5, 5);
                g2.draw(r);
                g2.fill(r);
            }

            g2.setColor(Color.lightGray);
            f = new Font(FONT_NAME, Font.BOLD, (int)(height*0.033));
            int w = (int)f.getStringBounds("LOCAL", g2.getFontRenderContext()).getWidth();
            int h = (int)f.getStringBounds("LOCAL", g2.getFontRenderContext()).getHeight();
            g2.setFont(f);
            int[] xpos = new int[] { width / 2 + 30 + wp / 2 - w / 2,  width / 2 - 30 - wh/2-w/2};
            g2.drawString("LOCAL", networkGameType == NetworkGameType.MASTER? xpos[1] : xpos[0], 2*h);
            w = (int)f.getStringBounds("REMOTO", g2.getFontRenderContext()).getWidth();
            g2.drawString("REMOTO", networkGameType == NetworkGameType.MASTER? xpos[0] : xpos[1], 2*h);

        } catch (NullPointerException exc) {

        }
        g2.dispose();
    }

    void moveBall() {
        if (points == 10)
            jackpot("Fin del Juego\nDesea seguir jugando?", "Has ganado!");
        else if (_points == 10)
            jackpot("Fin del Juego\nDesea seguir jugando?", "Has perdido!");

        if (ball.getBounds().intersectsLine(new Line2D.Double(0, height, width, height))) { // bounces bottom
            movement = 0;
            clipContact.play();
        } else if (ball.getBounds().intersectsLine(new Line2D.Double(0, 0, width, 0))) { // bounces top
            movement = 1;
            clipContact.play();
        } else if (ball.getBounds().intersectsLine(new Line2D.Double(width, 0, width, height)))    // removed -11 offset
        {
            if (!ball.getBounds().intersects(ai)) {
                points++;
                printScore();
                new GameScore(clipScore).run(); //utilizzo oggetto esterno causa thread.
                movement = 2;
            } else {
                if (ball.getBounds().intersectsLine(ai.getX(), ai.getY(), ai.getX(), (ai.getY() + ai.height/3)))
                    movement = 4;
                else if (ball.getBounds().intersectsLine(ai.getX(), ai.getY() + ai.height/3, ai.getX(), (ai.getY() + 2*ai.height/3)))
                    movement = 5;
                else if (ball.getBounds().intersectsLine(ai.getX(), ai.getY() + 2*ai.height/3, ai.getX(), (ai.getY() + ai.height)))
                    movement = 6;
            }
            clipContact.play();

        } else if (ball.getBounds().intersectsLine(new Line2D.Double(0, 0, 0, height))) { // removed -5 offset
            if (!ball.getBounds().intersects(player)) {
                _points++;
                printScore();
                new GameScore(clipScore).run(); //utilizzo oggetto esterno causa thread.
                movement = 3;
            } else {
                if (ball.getBounds().intersectsLine(player.getX(), player.getY(), player.getX(), (player.getY() + player.height/3)))
                    movement = 7;
                else if (ball.getBounds().intersectsLine(player.getX(), player.getY() + player.height/3, player.getX(), (player.getY() + 2*player.height/3)))
                    movement = 8;
                else if (ball.getBounds().intersectsLine(player.getX(), player.getY() + 2*player.height/3, player.getX(), (player.getY() + player.height)))
                    movement = 9;
            }
            clipContact.play();
        }

        switch (movement) {
            case 0:
                ball.moveY(false);
                ball.moveX(right);
                down = false;
                break;
            case 1:
                ball.moveY(true);
                ball.moveX(right);
                down = true;
                break;
            case 2:
                ball.moveX(false);
                ball.moveY(down);
                right = false;
                break;
            case 3:
                ball.moveX(true);
                ball.moveY(down);
                right = true;
                break;
            case 4:
                right = false;
                if (ball.getX() >= ai.getX()) down = false;
                ball.moveY(down);
                ball.moveX(right);
                break;
            case 5:
                right = false;
                ball.setSpeed(9);
                ball.moveX(right);
                ball.setSpeed(6);
                break;
            case 6:
                right = false;
                if (ball.getX() >= ai.getX()) down = true;
                ball.moveY(down);
                ball.moveX(right);
                break;
            case 7:
                right = true;
                if (ball.getX() <= player.getX() + player.width) down = false; // ball was not going up
                ball.moveY(down);
                ball.moveX(right);
                break;
            case 8:
                right = true;
                ball.setSpeed(9);
                ball.moveX(right);
                ball.setSpeed(6);
                break;
            case 9:
                right = true;
                if (ball.getX() <= player.getX() + player.width) down = true; // ball was not going up
                ball.moveY(down);
                ball.moveX(right);
                break;
            default:
                ball.moveX(true);
                ball.moveY(true);
                down = true;
                right = true;
                break;
        }
    }

    private void printScore() {
        aiTimer.stop();
        playerTimer = new Timer(SCORE_SHOW_DELAY, this); // TODO: 300
        playerTimer.start();
        ballMovement = false;
    }

    private void reset() {
        ball = getBall();
        ball.setSpeed((int) (height * BALL_SPEED)); // 6
        points = 0;
        _points = 0;
        down = networkGameType == NetworkGameType.MASTER;
        player.setY(0);
        ai.setY(0);
        repaint();
    }

    private void jackpot(String msg, String ttl) {
        if (JOptionPane.showConfirmDialog(this, msg, ttl,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            reset();
        else {
            parent.dispose();
            System.exit(0);
        }
    }

    private void updateStatus() {
        if (keys[KeyEvent.VK_R]) reset();

        if (player.getY() >= 2)
            if (keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_Q]) {
                player.move(false);
            }
        if (player.getY() <= (height - 50))
            if (keys[KeyEvent.VK_DOWN] || keys[KeyEvent.VK_A]) {
                player.move(true);
            }
    }

    private void updateRemote() {
        if (remoteKeys[KeyEvent.VK_R]) reset();

        if (ai.getY() >= 2)
            if (remoteKeys[KeyEvent.VK_UP] || remoteKeys[KeyEvent.VK_Q]) {
                ai.move(false);
            }
        if (ai.getY() <= (height - 50))
            if (remoteKeys[KeyEvent.VK_DOWN] || remoteKeys[KeyEvent.VK_A]) {
                ai.move(true);
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(aiTimer)) {
            if (ballMovement) {
                updateStatus();
                updateRemote();
                moveBall();
                repaint();
            }
        } else if (e.getSource().equals(playerTimer)) {
            repaint();
            ballMovement = true;
            playerTimer.stop();
            aiTimer.start();
        }
    }

    private class MyKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            keys[e.getKeyCode()] = true;
            if (e.getKeyChar() == (char)27) {
                aiTimer.stop();
                if (JOptionPane.showConfirmDialog(null, "Juego en progreso\n" +
                                "Desea continuar jugando?", "Pausa",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    aiTimer.start();
                else {
                    parent.dispose();
                    System.exit(0);
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            keys[e.getKeyCode()] = true;
            // add key to queue for thread to send
            keystr.add("true:" + e.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            keys[e.getKeyCode()] = false;
            // add key to queue for thread to send
            keystr.add("false:"+e.getKeyCode());
        }
    }

    private class MyMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
            if (isMouseDown) {
                int ny = e.getY();
                if (ny > mouseY) {
                    keys[KeyEvent.VK_DOWN] = true;
                    updateStatus();
                    keys[KeyEvent.VK_DOWN] = false;
                } else if (ny < mouseY) {
                    keys[KeyEvent.VK_UP] = true;
                    updateStatus();
                    keys[KeyEvent.VK_UP] = false;
                }
                mouseY = ny;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    private class MyMouseWheelListener implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int a = e.getWheelRotation();
            if (a < 0) {
                for(int i = 0; i < -a; i++) {
                    keys[KeyEvent.VK_UP] = true;
                    updateStatus();
                    keys[KeyEvent.VK_UP] = false;
                }
            }
            else if (a > 0) {
                for(int i = 0; i < a; i++) {
                    keys[KeyEvent.VK_DOWN] = true;
                    updateStatus();
                    keys[KeyEvent.VK_DOWN] = false;
                }
            }
        }
    }

    private class MyMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            isMouseDown = true;
            mouseY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isMouseDown = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
