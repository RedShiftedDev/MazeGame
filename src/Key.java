import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class Key {
    private final double x;
    private final double y;
    private int currentFrame = 0;
    private boolean isCollected = false;
    private long lastFrameTime = 0;
    private final List<BufferedImage> animationFrames;

    public Key(double x, double y, List<BufferedImage> animationFrames) {
        this.x = x;
        this.y = y;
        this.animationFrames = animationFrames;
        Random random = new Random();
        this.lastFrameTime = System.currentTimeMillis() + random.nextInt(690); // Random offset up to 1 second
    }

    public void updateAnimation(double deltaTime) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastFrameTime + 250) {
            currentFrame = (currentFrame + 1) % animationFrames.size();
            lastFrameTime = currentTime;
        }
    }

    public void collect() {
        isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public BufferedImage getImage() {
        return animationFrames.get(currentFrame);
    }
}