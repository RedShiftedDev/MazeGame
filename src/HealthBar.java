import java.awt.image.BufferedImage;
import java.util.List;

public class HealthBar {
    private final int maxHP;
    private int currentHP;
    private int spriteSheetFrame;
    private final List<BufferedImage> healthBarFrames;

    public HealthBar(int maxHP, List<BufferedImage> healthBarFrames) {
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.healthBarFrames = healthBarFrames;
        this.spriteSheetFrame = 0; // Start at full health frame
    }

    public void reduceHP(int amount, Hero hero) {
        currentHP = Math.max(0, currentHP - amount);
        System.out.println("Reducing HP by: " + amount + ", Current HP: " + currentHP); // Debug

        // Update the frame ONLY when the hero is VULNERABLE
        if (hero.isHurt()) {
            updateSpriteSheetFrame();
        }
    }

    private void updateSpriteSheetFrame() {
        // Calculate the number of FULL hearts to display
        spriteSheetFrame = Math.min(10, (100 - currentHP) / 10);
    }

    // Getters
    public int getCurrentHP() {
        return currentHP;
    }

    public BufferedImage getImage() {
        return healthBarFrames.get(spriteSheetFrame);
    }
}