import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {
    private final GameMenu gameMenu;
    public SettingsPanel(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        setLayout(new BorderLayout());

        JTextArea creditsText = new JTextArea("Game by:\nYour Name\n\nSpecial Thanks To:\n...");
        creditsText.setEditable(false); // Make it read-only
        add(creditsText, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameMenu.showMenu("main"); // Go back to the main menu
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
