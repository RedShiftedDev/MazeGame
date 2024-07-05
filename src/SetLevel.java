import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetLevel extends JPanel {

    private final GameMenu gameMenu;

    public SetLevel(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel titleLabel = new JLabel("Select Level");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, gbc);

        // Create buttons for each level
        JButton level1Button = createLevelButton("Level 1", 1);
        add(level1Button, gbc);

        JButton level2Button = createLevelButton("Level 2", 2);
        add(level2Button, gbc);

        JButton level3Button = createLevelButton("Level 3", 3);
        add(level3Button, gbc);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.showMenu("main");
            }
        });
        add(backButton, gbc);
    }

    // Helper method to create level buttons
    private JButton createLevelButton(String label, int level) {
        JButton button = new JButton(label);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.startGame(level); // Start game with selected level
            }
        });
        return button;
    }
}