package org.tauasa.apps.vietlatin;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Saves received audio bytes to disk and, if audio hardware is available,
 * plays them back immediately using the Java Sound API.
 *
 * <p>ElevenLabs returns MP3; Java Sound does not natively decode MP3 without a
 * codec library, so we save to disk and instruct the user to open the file if
 * direct playback is unavailable.</p>
 */
@Service
public class AudioPlayerService {

    private static final Logger log = LoggerFactory.getLogger(AudioPlayerService.class);

    /**
     * Persists audio bytes to the given path and attempts playback.
     *
     * @param audioBytes raw MP3 bytes from ElevenLabs
     * @param outputPath path where the MP3 file should be written
     */
    public void saveAndPlay(byte[] audioBytes, Path outputPath) {
        // 1. Save to disk
        try {
            Files.write(outputPath, audioBytes);
            log.info("Audio saved → {}", outputPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save audio file: {}", e.getMessage(), e);
            return;
        }

        // 2. Attempt playback via javax.sound
        //    Java Sound supports WAV/AU/AIFF natively; MP3 requires a plugin.
        //    We try, and fall back gracefully.
        try {
            playMp3(audioBytes);
        } catch (UnsupportedAudioFileException e) {
            // MP3 codec not present (common in headless / server JDKs)
            log.error("Direct playback unavailable (no MP3 codec). "
                    + "Open the file with your OS media player:");
            log.info("MP3 file: {}", outputPath.toAbsolutePath());
            printOpenCommand(outputPath);
        } catch (LineUnavailableException | IOException e) {
            log.error("Audio playback failed: {}", e.getMessage());
            log.info("Open the file manually: {}", outputPath.toAbsolutePath());
        }
    }

    // ──────────────────────────────────────────────────────────────

    private void playMp3(byte[] bytes)
            throws UnsupportedAudioFileException, LineUnavailableException, IOException {

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(
                new BufferedInputStream(new ByteArrayInputStream(bytes)))) {

            AudioFormat baseFormat   = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            try (AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, ais);
                 SourceDataLine line = AudioSystem.getSourceDataLine(decodedFormat)) {

                line.open(decodedFormat);
                line.start();

                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = decoded.read(buf)) != -1) {
                    line.write(buf, 0, bytesRead);
                }
                line.drain();
                log.info("Playback complete.");
            }
        }
    }

    private void printOpenCommand(Path path) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String abs = path.toAbsolutePath().toString();
        if (os.contains("mac")) {
            log.info("  $ open \"{}\"", abs);
        } else if (os.contains("win")) {
            log.info("  > start \"{}\"", abs);
        } else {
            log.info("  $ xdg-open \"{}\"", abs);
        }
    }
}
