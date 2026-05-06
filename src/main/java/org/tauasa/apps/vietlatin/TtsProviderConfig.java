package org.tauasa.apps.vietlatin;


import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers exactly one {@link TextToSpeechModel} bean based on the value of
 * {@code ongifier.tts.provider} in {@code application.properties}.
 *
 * <p>Supported values:
 * <ul>
 *   <li>{@code elevenlabs} (default) — requires {@code ELEVEN_LABS_API_KEY}</li>
 *   <li>{@code openai}               — requires {@code OPENAI_API_KEY}</li>
 * </ul>
 *
 * <p>Both provider starters ({@code spring-ai-starter-model-elevenlabs} and
 * {@code spring-ai-starter-model-openai}) are on the classpath. Spring AI's
 * auto-configuration creates a concrete model bean for each provider whose
 * API key is configured. This class takes the already-created concrete bean
 * and exposes it under the shared {@link TextToSpeechModel} interface, which
 * is what {@link OngifierService} depends on.
 *
 * <p>Switching providers requires only changing one property — no code changes.
 */
@Configuration
public class TtsProviderConfig {

    /**
     * Active when {@code ongifier.tts.provider=elevenlabs} (or when the
     * property is absent, since {@code matchIfMissing = true}).
     */
    @Bean
    @ConditionalOnProperty(
            name         = "ongifier.tts.provider",
            havingValue  = "elevenlabs",
            matchIfMissing = true
    )
    public TextToSpeechModel elevenLabsTtsModel(ElevenLabsTextToSpeechModel model) {
        return model;
    }

    /**
     * Active when {@code ongifier.tts.provider=openai}.
     */
    @Bean
    @ConditionalOnProperty(
            name        = "ongifier.tts.provider",
            havingValue = "openai"
    )
    public TextToSpeechModel openAiTtsModel(OpenAiAudioSpeechModel model) {
        return model;
    }
}
