import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOver extends JFrame {
    private JLabel gameOverLabel;
    private JLabel scoreLabel;
    private JButton retryButton;
    private JButton totalScoreButton;
    private JButton mainMenuButton;
    private MazePanel mazePanel;

    public GameOver(MazePanel mazePanel) {
        this.mazePanel = mazePanel;

        AudioPlayer.stopSound("backgroundMusic");

        setTitle("Game Over");
        setSize(300, 200); // Adjusted size for more buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setLocationRelativeTo(null);

        gameOverLabel = new JLabel("Game Over!");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(gameOverLabel);

        scoreLabel = new JLabel("");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(scoreLabel);

        retryButton = new JButton("Retry");
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mazePanel.resetGame();
                setVisible(false);
            }
        });
        add(retryButton);

        totalScoreButton = new JButton("Total Score");
        totalScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement showTotalScore() in MazePanel
                mazePanel.showTotalScore();
            }
        });
        add(totalScoreButton);

        mainMenuButton = new JButton("Main Menu");
        mainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement goToMainMenu() in MazePanel
                mazePanel.goToMainMenu();
            }
        });
        add(mainMenuButton);

        setVisible(false);
    }

    public void showGameOver(int score) {
        scoreLabel.setText("Your Score: " + score);
        setVisible(true);
    }
}