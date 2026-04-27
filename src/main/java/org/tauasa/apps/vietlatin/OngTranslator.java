package org.tauasa.apps.vietlatin;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Translates plain English text into "Ong dialect":
 * <ul>
 *   <li>Consonants get an "ong" suffix appended  (e.g. P → pong)</li>
 *   <li>Vowels are replaced with their macron equivalents
 *       (A→ā, E→ē, I→ī, O→ō, U→ū)</li>
 *   <li>Each translated character is separated by a space within a word</li>
 *   <li>Words are separated by two spaces so TTS pauses naturally</li>
 * </ul>
 *
 * Examples:
 *   Park  → pong ā rong kong
 *   Dog   → dong ō gong
 *   Hello → hong ē long long ō
 */
@Component
public class OngTranslator {

    private static final Map<Character, String> VOWEL_MAP = Map.of(
            'a', "ā",
            'e', "ē",
            'i', "ī",
            'o', "ō",
            'u', "ū"
    );

    /**
     * Translates the supplied text and returns both a display-friendly string
     * and a TTS-optimised string.
     */
    public TranslationResult translate(String input) {
        String[] words = input.trim().split("\\s+");

        List<String> translatedWords = new ArrayList<>();

        for (String word : words) {
            List<String> translatedChars = new ArrayList<>();

            for (char c : word.toCharArray()) {
                String translated = translateChar(c);
                if (translated != null) {
                    translatedChars.add(translated);
                }
                // non-alphabetic characters are silently dropped from speech
            }

            if (!translatedChars.isEmpty()) {
                translatedWords.add(String.join(" ", translatedChars));
            }
        }

        // Display version: words separated by "  |  " for readability
        String display = String.join("   |   ", translatedWords);

        // TTS version: words separated by ", " so ElevenLabs pauses between words
        String tts = String.join(", ", translatedWords);

        return new TranslationResult(input, display, tts, translatedWords);
    }

    /**
     * Translates a single character.
     *
     * @return the translated token, or {@code null} if the character should be skipped
     */
    private String translateChar(char c) {
        char lower = Character.toLowerCase(c);

        if (!Character.isLetter(c)) {
            return null; // punctuation, digits, spaces — skip
        }

        // Vowel?
        if (VOWEL_MAP.containsKey(lower)) {
            return VOWEL_MAP.get(lower);
        }

        // Consonant — append "ong"
        return lower + "ong";
    }

    // ──────────────────────────────────────────────────────────────
    // Result record
    // ──────────────────────────────────────────────────────────────

    public record TranslationResult(
            String original,
            String display,
            String tts,
            List<String> words
    ) {}
}
