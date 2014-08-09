import java.awt.geom.Rectangle2D;

/**
 * @author Emil
 */
public class AIPaddle extends Rectangle2D.Double {
    private final int gameHeight, gameWidth;
    private final double  SPEED;

    public AIPaddle(int x, int y, double speed, int gameWidth, int gameHeight, int width, int height) {
        SPEED = speed;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.gameHeight = gameHeight;
        this.gameWidth = gameWidth;
    }

    public void moveAi(double ballX, double ballY, double ballW, double ballH) {
        if (ballX >= gameWidth / 2) {
            if (ballY < y && y >= 2)
                y -= SPEED;
            if (ballY > y && y + height <= gameHeight + ballH)
                y += SPEED;
        }
    }

}