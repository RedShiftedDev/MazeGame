import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AudioPlayer {

    private static final Map<String, Clip> clips = new HashMap<>();
    private static float masterVolume = 1.0f; // Master volume level (1.0f is full volume)

    public static void loadSound(String soundName, String filePath) {
        try {
            URL url = AudioPlayer.class.getResource(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(Objects.requireNonNull(url));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clips.put(soundName, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + filePath + " - " + e.getMessage());
        }
    }

    public static void playSound(String soundName) {
        Clip clip = clips.get(soundName);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0); // Rewind to the beginning
            setClipVolume(clip, masterVolume); // Apply master volume to the clip
            clip.start();
        }
    }

    public static void loopSound(String soundName) {
        Clip clip = clips.get(soundName);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            if (soundName.equals("backgroundMusic")) { // 50% volume for background music
                setClipVolume(clip, masterVolume * 0.2f); // Apply 50% of master volume
            } else {
                setClipVolume(clip, masterVolume); // Full volume for other sounds
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopSound(String soundName) {
        Clip clip = clips.get(soundName);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static boolean isSoundPlaying(String soundName) {
        Clip clip = clips.get(soundName);
        return clip != null && clip.isRunning();
    }

    public static void setVolume(int volume) {
        masterVolume = (float) volume / 100; // Update master volume
        for (Clip clip : clips.values()) {
            if (clip != null) {
                setClipVolume(clip, masterVolume);
            }
        }
    }

    // Helper method to set volume for a Clip
    private static void setClipVolume(Clip clip, float volume) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (gainControl != null) {
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }
}