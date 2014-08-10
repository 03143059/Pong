import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;


public class GameScreen extends JPanel implements ActionListener {

    private static final Boolean DEBUG = true;
    public static final double HORIZONTAL_SCALE = 0.0125;
    public static final double VERTICAL_SCALE = 0.12;
    public static final double BALL_SCALE = 0.025;
    public static final String FONT_NAME = "Texas LED";// "8BIT WONDER";

    private Timer timer, _timer;
    private int movement, points, _points, score, scorePX, scoreCX, scoreY;
    private int width, height;
    private Ball ball;
    private final Paddle player;
    private final AIPaddle ai;
    private boolean right, down, ballMovement;
    private boolean[] keys;
    private final URL url, _url;
    private final AudioClip clipContact, clipScore;
    private final double SPEED_AI = 5.5;
    JFrame parent;
    private boolean isMouseDown;
    private int mouseY;

    public GameScreen(JFrame parent) {
        this.parent = parent;
        this.width = parent.getContentPane().getWidth();
        this.height = parent.getContentPane().getHeight();
        super.setSize(width, height);
        this.width -= 10;
        super.setBackground(Color.BLACK);
        keys = new boolean[256];
        for (int i = 0; i < keys.length; i++)
            keys[i] = false;
        parent.addKeyListener(new MyKeyListener());
        parent.addMouseWheelListener(new MyMouseWheelListener());
        parent.addMouseListener(new MyMouseListener());
        parent.addMouseMotionListener(new MyMouseMotionListener());
        setFocusable(true);
        score = 0;
        points = 0;
        _points = 0;
        movement = 10;
        scorePX = width / 6;
        scoreCX = 430;
        scoreY = height/5;
        ball = getBall();
        ball.setSpeed(6);
        player = getPaddle();
        player.setSpeed(7);
        ai = getAiPaddle();
        url = Pong.class.getResource("pongLimit.wav");
        _url = Pong.class.getResource("pongScore.wav");
        clipContact = Applet.newAudioClip(url);
        clipScore = Applet.newAudioClip(_url);
        timer = new Timer(10, this);
        timer.start();
        ballMovement = true;
    }

    private AIPaddle getAiPaddle() {
        return new AIPaddle(width-(int)(HORIZONTAL_SCALE * width), 0, SPEED_AI, width, height, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
    }

    private Paddle getPaddle() {
        return new Paddle(0, 0, (int)(HORIZONTAL_SCALE * width), (int)(VERTICAL_SCALE * height));
    }

    private Ball getBall() {
        return new Ball(100, 40, (int)(BALL_SCALE * width), (int)(BALL_SCALE * width));
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
            setDimension(g2);
            g2.drawString("" + _points, width / 2 + 30, scoreY);
            int w = (int)f.getStringBounds(""+points, g2.getFontRenderContext()).getWidth();
            g2.drawString("" + points, width / 2 - w-30, scoreY);
            for (int i = 0; i < height + 10; i += 10) {
                Rectangle2D.Double r = new Rectangle2D.Double(width / 2, i, 5, 5);
                g2.draw(r);
                g2.fill(r);
            }
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
                score = 1;
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
                score = 2;
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
        timer.stop();
        _timer = new Timer(300, this);
        _timer.start();
        ballMovement = false;
    }

    private void reset() {
        ball = getBall();
        ball.setSpeed(6);
        points = 0;
        _points = 0;
        player.setY(0);
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

    void setDimension(Graphics2D g2)                                 //check the presence of a custom font
    {
        if (!g2.getFont().getName().equals(FONT_NAME)) {
            g2.setFont(new Font("Serif", Font.BOLD, 50));
            scoreY = 40;
            scorePX = width / 5;
            scoreCX = 450;
        }
    }

    private void updateStatus() {
        if (player.getY() >= 2)
            if (keys[KeyEvent.VK_UP]) {
                player.move(false);
            }
        if (player.getY() <= (height - 50))
            if (keys[KeyEvent.VK_DOWN]) {
                player.move(true);
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(timer)) {
            if (ballMovement) {
                updateStatus();
                ai.moveAi(ball.x, ball.y, ball.width, ball.height);
                moveBall();
                repaint();
            }
        } else if (e.getSource().equals(_timer)) {
            repaint();
            score = 0;
            ballMovement = true;
            _timer.stop();
            timer.start();
        }
    }

    private class MyKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            keys[e.getKeyCode()] = true;
            if (e.getKeyChar() == (char)27) {
                timer.stop();
                if (JOptionPane.showConfirmDialog(null, "Juego en progreso\n" +
                                "Desea continuar jugando?", "Pausa",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    timer.start();
                else {
                    parent.dispose();
                    System.exit(0);
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            keys[e.getKeyCode()] = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            keys[e.getKeyCode()] = false;
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
