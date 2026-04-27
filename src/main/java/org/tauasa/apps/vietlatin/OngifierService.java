package org.tauasa.apps.vietlatin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Orchestrates the full pipeline:
 * <ol>
 *   <li>Translate input text into Ong dialect</li>
 *   <li>Print the translation to stdout</li>
 *   <li>Call ElevenLabs TTS via Spring AI</li>
 *   <li>Save and play the returned audio</li>
 * </ol>
 */
@Service
public class OngifierService {

    private static final Logger log = LoggerFactory.getLogger(OngifierService.class);

    private final OngTranslator translator;
    private final ElevenLabsTextToSpeechModel ttsModel;
    private final AudioPlayerService audioPlayer;

    @Value("${ongifier.output-file:output.mp3}")
    private String outputFile;

    public OngifierService(OngTranslator translator,
                           ElevenLabsTextToSpeechModel ttsModel,
                           AudioPlayerService audioPlayer) {
        this.translator  = translator;
        this.ttsModel    = ttsModel;
        this.audioPlayer = audioPlayer;
    }

    /**
     * Full pipeline: translate → display → speak.
     *
     * @param input raw English text entered by the user
     */
    public void process(String input) {
        // ── 1. Translate ─────────────────────────────────────────────
        OngTranslator.TranslationResult result = translator.translate(input);

        // ── 2. Display ───────────────────────────────────────────────
        System.out.println();
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  Original  : " + result.original());
        System.out.println("  Ong form  : " + result.display());
        System.out.println("═══════════════════════════════════════════");
        System.out.println();

        // ── 3. Build TTS prompt ───────────────────────────────────────
        // We send the TTS-optimised string (comma-separated words) so
        // ElevenLabs adds natural pauses at word boundaries.
        String ttsText = result.tts();
        log.debug("Sending to ElevenLabs TTS: {}", ttsText);

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(
                ttsText,
                ElevenLabsTextToSpeechOptions.builder()
                        // Override options at runtime if desired; otherwise the
                        // defaults set in application.properties are used.
                        .build()
        );

        // ── 4. Call TTS API ───────────────────────────────────────────
        log.info("Calling ElevenLabs TTS API…");
        TextToSpeechResponse response = ttsModel.call(prompt);
        byte[] audioBytes = response.getResult().getOutput();
        log.info("Received {} bytes of audio.", audioBytes.length);

        // ── 5. Save & play ────────────────────────────────────────────
        Path outputPath = Paths.get(outputFile);
        audioPlayer.saveAndPlay(audioBytes, outputPath);
    }
}
