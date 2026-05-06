package org.tauasa.apps.vietlatin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Orchestrates the full pipeline:
 * <ol>
 *   <li>Translate input text into Ong dialect</li>
 *   <li>Print the translation to stdout</li>
 *   <li>Call the configured TTS provider via Spring AI's shared interface</li>
 *   <li>Save and play the returned audio</li>
 * </ol>
 *
 * <p>The active TTS provider is controlled entirely by a single property:
 * <pre>
 *   ongifier.tts.provider=elevenlabs   # or: openai
 * </pre>
 *
 * <p>{@link OngifierService} depends only on the {@link TextToSpeechModel}
 * interface. The concrete implementation ({@link ElevenLabsTextToSpeechModel}
 * or {@link OpenAiAudioSpeechModel}) is wired by {@link TtsProviderConfig}.
 */
@Service
public class OngifierService {

    private static final Logger log = LoggerFactory.getLogger(OngifierService.class);

    private final OngTranslator translator;
    private final TextToSpeechModel ttsModel;       // provider-agnostic interface
    private final AudioPlayerService audioPlayer;

    @Value("${ongifier.output-file:output.mp3}")
    private String outputFile;

    @Value("${ongifier.tts.provider:elevenlabs}")
    private String provider;

    public OngifierService(OngTranslator translator,
                           TextToSpeechModel ttsModel,
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
        // ── 1. Translate ──────────────────────────────────────────────
        OngTranslator.TranslationResult result = translator.translate(input);

        // ── 2. Display ────────────────────────────────────────────────
        System.out.println();
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  Original  : " + result.original());
        System.out.println("  Ong form  : " + result.display());
        System.out.println("  Provider  : " + provider);
        System.out.println("═══════════════════════════════════════════");
        System.out.println();

        // ── 3. Build a provider-appropriate prompt ────────────────────
        // Both providers implement TextToSpeechModel, so the call() contract
        // is identical. Provider-specific options are set here; runtime
        // defaults fall back to application.properties values if not
        // overridden.
        TextToSpeechPrompt prompt = buildPrompt(result.tts());

        // ── 4. Call TTS API ───────────────────────────────────────────
        log.info("Calling {} TTS API…", provider);
        TextToSpeechResponse response = ttsModel.call(prompt);
        byte[] audioBytes = response.getResult().getOutput();
        log.info("Received {} bytes of audio from {}.", audioBytes.length, provider);

        // ── 5. Save & play ────────────────────────────────────────────
        Path outputPath = Paths.get(outputFile);
        audioPlayer.saveAndPlay(audioBytes, outputPath);
    }

    /**
     * Constructs a {@link TextToSpeechPrompt} with options appropriate for
     * whichever provider is currently active.
     *
     * <p>Options set here are <em>runtime overrides</em>. If a field is left
     * unset (i.e. the builder is called with no arguments), Spring AI falls
     * back to the values in {@code application.properties}.
     *
     * <p>Add provider-specific overrides here as needed — for example,
     * choosing a different voice per word length, adjusting stability for
     * dramatic effect, etc.
     */
    private TextToSpeechPrompt buildPrompt(String ttsText) {
        log.debug("Sending to {} TTS: {}", provider, ttsText);

        if (ttsModel instanceof ElevenLabsTextToSpeechModel) {
            return new TextToSpeechPrompt(
                    ttsText,
                    ElevenLabsTextToSpeechOptions.builder()
                            // Uncomment to override application.properties at runtime:
                            // .voiceId("different-voice-id")
                            // .model("eleven_multilingual_v2")
                            .build()
            );
        }

        if (ttsModel instanceof OpenAiAudioSpeechModel) {
            return new TextToSpeechPrompt(
                    ttsText,
                    OpenAiAudioSpeechOptions.builder()
                            // Uncomment to override application.properties at runtime:
                            // .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
                            // .model(OpenAiAudioApi.TtsModel.TTS_1_HD.value)
                            // .speed(0.9)
                            .build()
            );
        }

        // Fallback: send text with no provider-specific options.
        // The provider's auto-configured defaults will be used.
        log.warn("Unknown TTS provider type {}; using default options.", ttsModel.getClass().getSimpleName());
        return new TextToSpeechPrompt(ttsText);
    }
}