import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameMenu extends JPanel {
    private final JFrame frame;

    private static final int SCREEN_WIDTH = 1000;  // Desired screen width
    private static final int SCREEN_HEIGHT = 800; // Desired screen height
    private final MainMenuPanel mainMenu;
    private boolean mainMenuShown = false; // Flag to track if shown

    public GameMenu(JFrame frame) {
        this.frame = frame;
        setLayout(new CardLayout());

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        mainMenu = new MainMenuPanel(this); // Now it's not null
        CreditsPanel creditsMenu = new CreditsPanel(this);
        SettingsPanel settingsMenu = new SettingsPanel(this);

        // Add panels to the card layout
        add(mainMenu, "main");
        add(creditsMenu, "credits");
        add(settingsMenu, "settings");

        loadSound();
        AudioPlayer.loopSound("bgc");

    }

    private void loadSound(){
        try {
            AudioPlayer.loadSound("bgc", "/assets/sounds/backgroundSound.wav");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void showMenu(String menuName) {
        if (menuName.equals("levelSelect")) {
            SetLevel levelSelectMenu = new SetLevel(this);
            add(levelSelectMenu, "levelSelect");
        } else if (menuName.equals("main")) {
            if (!mainMenuShown) { // Check flag here
                mainMenuShown = true;
                mainMenu.initializeStars();
            }
        }
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, menuName);
    }

    public void startGame(int level) {
        // This method is now called from the SetLevel panel
        frame.remove(this);
        int[][] maze = MazeLogic.generateMaze(10, 20); // Create the maze here
        MazePanel mazePanel = new MazePanel(maze, 10, 20, level); // Pass maze data
        frame.add(mazePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        mazePanel.setFocusable(true);
        mazePanel.requestFocusInWindow();
        frame.validate(); // Refresh the frame content
        frame.repaint();
    }
}