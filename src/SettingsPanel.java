import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {
    private final GameMenu gameMenu;
    public SettingsPanel(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        setLayout(new BorderLayout());

        // In your MazePanel (or a settings panel if you have one):
        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50); // Range 0-100, initial value 50
        volumeSlider.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        volumeSlider.setMajorTickSpacing(25); // Set tick marks
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        volumeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int volume = source.getValue();
                AudioPlayer.setVolume(volume); // Call a method to set the volume in AudioPlayer
            }
        });

        // Add the slider to your UI layout (using a suitable layout manager)
        add(volumeSlider);

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
