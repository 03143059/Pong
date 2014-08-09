import java.awt.geom.Ellipse2D;

/**
 * @author daniele
 */
public class Ball extends Ellipse2D.Double {

    private int speed;

    public Ball(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean setSpeed(int speed) {
        if (speed < 0 || speed > 10)
            return false;
        this.speed = speed;
        return true;
    }

    void moveX(boolean right) {
        if (right)
            x += speed;
        else x -= speed;
    }

    void moveY(boolean down) {
        if (down)
            y += speed;
        else y -= speed;
    }

}
