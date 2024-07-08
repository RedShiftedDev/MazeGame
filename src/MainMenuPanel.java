import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MainMenuPanel extends JPanel {

    private BufferedImage planetSpriteSheet;
    private BufferedImage[] starImages; // Array to hold individual star images
    private int currentFrame = 0;
    private int frameWidth;
    private int frameHeight;
    private int numFrames;
    private static final int NUM_STARS = 500; // Adjust the number of stars
    private final List<Star> stars = new ArrayList<>();
    private static final int NUM_SHOOTING_STARS = 1; // Adjust as needed
    private final List<ShootingStar> shootingStars = new ArrayList<>();
    private final Random random = new Random();
    private boolean starsInitialized = false; // Flag to track initialization

    public MainMenuPanel(GameMenu gameMenu) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        GradientLabel titleLabel = new GradientLabel("Maze Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(titleLabel, gbc);


        // Add a Timer to trigger repaints:
        Timer animationTimer = new Timer(90, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFrame = (currentFrame + 1) % numFrames; // Cycle through frames
                repaint();
            }
        });
        animationTimer.start();

        // Load the planet image
        try {
            BufferedImage starSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/stars/stars.png")));
            int numStars = 11;
            starImages = new BufferedImage[numStars];
            int starWidth = starSpriteSheet.getWidth() / numStars;
            int starHeight = starSpriteSheet.getHeight();
            for (int i = 0; i < numStars; i++) {
                starImages[i] = starSpriteSheet.getSubimage(i * starWidth, 0, starWidth, starHeight);
            }
            planetSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/planet/planet.png"))); // Replace with your image path
            numFrames = 50;
            frameWidth = planetSpriteSheet.getWidth() / numFrames; // Assuming frames are horizontally arranged
            frameHeight = planetSpriteSheet.getHeight();
        } catch (IOException e) {
            System.err.println("Error loading planet image: " + e.getMessage());
        }

        // Add a ComponentListener to detect size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                starsInitialized = false; // Force reinitialization of stars
                initializeStars();
                repaint();
            }
        });

        // Start Button Now Shows Level Selection
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.showMenu("levelSelect"); // Go to level selection
            }
        });
        add(startButton, gbc);

        JButton creditsButton = new JButton("Credits");
        creditsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.showMenu("credits");
            }
        });
        add(creditsButton, gbc);

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.showMenu("settings");
            }
        });
        add(settingsButton, gbc);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(quitButton, gbc);

    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!starsInitialized) {
            initializeStars();
            starsInitialized = true;
        }

        Graphics2D g2d = (Graphics2D) g;

        // 1. Create the space gradient:
        Color spaceColor1 = new Color(19, 19, 46); // Dark blue
        Color spaceColor2 = new Color(0, 0, 25);   // Even darker blue (or black)
        GradientPaint spaceGradient = new GradientPaint(0, 0, spaceColor1,
                0, getHeight(), spaceColor2);

        // 2. Fill background with gradient:
        g2d.setPaint(spaceGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 3. Draw and update stars:
        for (Star star : stars) {
            star.draw(g2d);
            star.updateTwinkle();
        }

        // 4. Draw shooting stars:
        for (ShootingStar shootingStar : shootingStars) {
            shootingStar.draw(g2d);
        }

        // 5. Draw the planet:
        if (planetSpriteSheet != null) {
            int planetX = getWidth() / 4;
            int planetY = getHeight() / 3;
            int frameX = currentFrame * frameWidth; // X-coordinate of the current frame in the sprite sheet
            g2d.drawImage(planetSpriteSheet.getSubimage(frameX, 0, frameWidth, frameHeight), planetX - 100, planetY - 50, frameWidth * 2, frameHeight * 2, null);
        }
    }

    void initializeStars() {
        // Clear the existing stars before reinitializing
        stars.clear();
        shootingStars.clear();

        for (int i = 0; i < NUM_STARS; i++) {
            stars.add(new Star());
        }
        for (int i = 0; i < NUM_SHOOTING_STARS; i++) {
            shootingStars.add(new ShootingStar());
        }
    }

    // GradientLabel class to be used for title label
    private static class GradientLabel extends JLabel {
        public GradientLabel(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Ensure background is rendered first

            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();

            // Create a gradient from blue to light blue
            GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, height, Color.CYAN);
            g2d.setPaint(gradient);

            // Get the font metrics for the label's text
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(getText(), g2d);

            // Calculate the x and y positions to center the text
            int x = (width - (int) rect.getWidth()) / 2;
            int y = (height - (int) rect.getHeight()) / 2 + fm.getAscent();

            // Draw the text with the gradient
            g2d.drawString(getText(), x, y);
        }
    }


    // Inner class for individual stars
    private class Star {
        int x;
        int y;
        int size;
        float alpha; // For twinkling effect
        float alphaChange; // To change alpha over time
        int starImageIndex; // Index to select from starImages array
        BufferedImage starImage; // The current star image

        public Star() {
            x = random.nextInt(getWidth());
            y = random.nextInt(getHeight());
            size = random.nextInt(3) + 1; // Star size variation
            alpha = random.nextFloat();
            alphaChange = 0.02f * (random.nextBoolean() ? 1 : -1);
            starImageIndex = random.nextInt(starImages.length); // Randomly choose a star image
            starImage = starImages[starImageIndex];
        }

        public void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Adjust the scaling factor (2.0 in this example) to control the size:
            int scaledSize = (int) (size * 2.0);
            g2d.drawImage(starImage,
                    x,
                    y,
                    scaledSize * starImage.getWidth() / starImage.getHeight(), // Maintain aspect ratio
                    scaledSize,
                    null);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        public void updateTwinkle() {
            alpha += alphaChange;
            // Clamp alpha to be between 0.0f and 1.0f
            alpha = Math.max(0.0f, Math.min(alpha, 1.0f));
            if (alpha >= 1.0f || alpha <= 0.2f) {
                alphaChange *= -1;
            }
        }
    }

    // Inner class for shooting stars
    private class ShootingStar {
        int x;
        int y;
        int length;
        double angle;
        float alpha = 1.0f;
        int speed;

        int delay = 0; // Delay counter
        int delayTime = random.nextInt(25) + 69; // Random delay of 25 to 69 frames

        public ShootingStar() {
            reset();
        }

        private void reset() {
            x = random.nextInt(getWidth());
            y = random.nextInt(getHeight());
            length = random.nextInt(50) + 150; // Varying length
            angle = random.nextDouble() * Math.PI * 2; // Random direction
            speed = random.nextInt(35) + 30; // Varying speed
            alpha = 1.0f;
        }

        public void draw(Graphics2D g2d) {

            // Update delay counter
            if (delay > 0) {
                delay--;
                return; // Don't draw or update position if in delay
            }

            int x2 = (int) (x + length * Math.cos(angle));
            int y2 = (int) (y + length * Math.sin(angle));

            // 1. Draw the glow (thicker, semi-transparent line):
            g2d.setColor(new Color(1.0f, 0.8f, 0.6f, 0.1f)); // Semi-transparent white
            Stroke glowStroke = new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(glowStroke);
            g2d.drawLine(x, y, x2, y2);

            // 2. Draw the main shooting star line (as before):
            GradientPaint gradient = new GradientPaint(
                    x, y, new Color(1.0f, 1.0f, 1.0f, 0.0f),
                    x2, y2, new Color(1.0f, 1.0f, 1.0f, 1.0f)
            );
            g2d.setPaint(gradient);
            Stroke starStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(starStroke);
            g2d.drawLine(x, y, x2, y2);

            // Update position and alpha for animation
            x += (int) (speed * Math.cos(angle));
            y += (int) (speed * Math.sin(angle));
            alpha -= 0.02f; // Fade out

            // Reset if off-screen or faded
            if (x < 0 || x > getWidth() || y < 0 || y > getHeight() || alpha <= 0) {
                delay = delayTime; // Start the delay
                reset();
            }
        }
    }
}