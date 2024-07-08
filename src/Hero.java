import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Hero {
    private int row, col;
    double x;
    double y;
    final double speed;
    private int health;
    private boolean hasKey = false;
    boolean movingUp;
    boolean movingDown;
    boolean movingLeft;
    boolean movingRight;
    private boolean isHurt = false;
    public enum HeroAnimationState { IDLE, WALKING_FORWARD, WALKING_BACKWARD }
    private HeroAnimationState currentState = HeroAnimationState.IDLE;
    private final MazePanel mazePanel;
    private final List<BufferedImage> idleFrames = new ArrayList<>();
    private final List<BufferedImage> walkForwardFrames = new ArrayList<>(); // For forward animation
    private final List<BufferedImage> walkBackwardFrames = new ArrayList<>(); // For backward animation
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final int numIdleFrames;
    private final int numWalkFrames;
    private long hurtStartTime = 0; // Time when the hero was last hurt
    private static final long INVULNERABILITY_DURATION = 1500;

    public Hero(int startX, int startY, BufferedImage idleSpriteSheet,
                BufferedImage walkForwardSpriteSheet, BufferedImage walkBackwardSpriteSheet,
                int speed, MazePanel mazePanel) {
        this.mazePanel = mazePanel;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.health = 100;
        this.numIdleFrames = 8;
        this.numWalkFrames = 4;
        loadAnimationFrames(idleSpriteSheet, HeroAnimationState.IDLE);
        loadAnimationFrames(walkForwardSpriteSheet, HeroAnimationState.WALKING_FORWARD); // Load forward frames
        loadAnimationFrames(walkBackwardSpriteSheet, HeroAnimationState.WALKING_BACKWARD); // Load backward frames
    }

    public void setMoving(int keyCode, boolean isMoving) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT -> movingLeft = isMoving;
            case KeyEvent.VK_RIGHT -> movingRight = isMoving;
            case KeyEvent.VK_UP -> movingUp = isMoving;
            case KeyEvent.VK_DOWN -> movingDown = isMoving;
        }

        // Update animation state based on movement
        if (isMoving) {
            if (keyCode == KeyEvent.VK_LEFT) {
                setState(HeroAnimationState.WALKING_BACKWARD); // Moving up means walking backward
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                setState(HeroAnimationState.WALKING_FORWARD); // Moving down means walking forward
            } else if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP) &&
                    currentState != HeroAnimationState.WALKING_FORWARD &&
                    currentState != HeroAnimationState.WALKING_BACKWARD) {
                // For left and right, if not already walking, default to forward
                setState(HeroAnimationState.WALKING_FORWARD);
            }
        } else if (currentState == HeroAnimationState.WALKING_FORWARD || currentState == HeroAnimationState.WALKING_BACKWARD) {
            // If stopped moving from a walking state, go back to idle
            setState(HeroAnimationState.IDLE);
        }
    }

    public boolean isHurt() {
        long currentTime = System.currentTimeMillis();
        return !isHurt || (currentTime - hurtStartTime >= INVULNERABILITY_DURATION);
    }

    public void setHurt(boolean hurt) {
        if (hurt) {
            hurtStartTime = System.currentTimeMillis();
        }
        isHurt = hurt;
    }

    public void collectKey() {
        hasKey = true;
    }

    public boolean hasKey() {
        return hasKey;
    }

    private void loadAnimationFrames(BufferedImage spriteSheet, HeroAnimationState state) {
        if (spriteSheet == null) return;

        int numFrames = switch (state) {
            case IDLE -> numIdleFrames;
            case WALKING_BACKWARD, WALKING_FORWARD -> numWalkFrames;
        };

        int frameWidth = spriteSheet.getWidth() / numFrames;
        int frameHeight = spriteSheet.getHeight();

        List<BufferedImage> frames;
        switch (state) {
            case IDLE -> frames = idleFrames;
            case WALKING_FORWARD -> frames = walkForwardFrames;
            case WALKING_BACKWARD -> frames = walkBackwardFrames;
            default -> {
                return; // Or throw an error if an invalid state is passed
            }
        }

        frames.clear();

        for (int i = 0; i < numFrames; i++) {
            frames.add(spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight));
        }
    }

    public void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        long frameTime = 200;

        if (currentTime - lastFrameTime >= frameTime) {
            switch (currentState) {
                case WALKING_FORWARD:
                    currentFrame = (currentFrame + 1) % walkForwardFrames.size();
                    break;
                case WALKING_BACKWARD:
                    currentFrame = (currentFrame + 1) % walkBackwardFrames.size();
                    break;
                default:
                    currentFrame = (currentFrame + 1) % idleFrames.size();
                    break;
            }
            lastFrameTime = currentTime;
        }
    }

    public double[] getBounds() {
        double centerX = x + MazePanel.CELL_SIZE / 2.0;
        double centerY = y + MazePanel.CELL_SIZE / 2.0;
        double radius = MazePanel.CELL_SIZE / 2.0;
        return new double[]{centerX, centerY, radius};
    }

    public void setState(HeroAnimationState newState) {
        // Only reset the frame if switching to a DIFFERENT animation state
        if (currentState != newState) {
            currentState = newState;
            currentFrame = 0; // Reset frame to the beginning of the new animation
        }
    }

    // Getters for x and y
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth(int amount) {
        health -= amount;
    }

    public BufferedImage getImage() {
        return switch (currentState) {
            case WALKING_FORWARD -> walkForwardFrames.get(currentFrame);
            case WALKING_BACKWARD -> walkBackwardFrames.get(currentFrame);
            default -> idleFrames.get(currentFrame);
        };
    }
}