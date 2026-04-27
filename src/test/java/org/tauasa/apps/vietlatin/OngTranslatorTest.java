package org.tauasa.apps.vietlatin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OngTranslator}.
 *
 * These tests exercise the pure translation logic with no Spring context
 * and no external API calls — fast and deterministic.
 */
class OngTranslatorTest {

    private OngTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new OngTranslator();
    }

    // ──────────────────────────────────────────────────────────────
    // Examples from the spec
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Park → pong ā rong kong")
    void testPark() {
        OngTranslator.TranslationResult r = translator.translate("Park");
        assertWordEquals("pong ā rong kong", r, 0);
    }

    @Test
    @DisplayName("Dog → dong ō gong")
    void testDog() {
        OngTranslator.TranslationResult r = translator.translate("Dog");
        assertWordEquals("dong ō gong", r, 0);
    }

    @Test
    @DisplayName("Hello → hong ē long long ō")
    void testHello() {
        OngTranslator.TranslationResult r = translator.translate("Hello");
        assertWordEquals("hong ē long long ō", r, 0);
    }

    // ──────────────────────────────────────────────────────────────
    // Vowel mappings
    // ──────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "Vowel {0} → {1}")
    @CsvSource({
            "a, ā",
            "e, ē",
            "i, ī",
            "o, ō",
            "u, ū",
            "A, ā",
            "E, ē",
            "I, ī",
            "O, ō",
            "U, ū"
    })
    void testVowelTranslation(String input, String expected) {
        OngTranslator.TranslationResult r = translator.translate(input);
        assertWordEquals(expected, r, 0);
    }

    // ──────────────────────────────────────────────────────────────
    // Consonant suffix
    // ──────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "Consonant {0} → {1}")
    @CsvSource({
            "b, bong",
            "c, cong",
            "d, dong",
            "f, fong",
            "g, gong",
            "h, hong",
            "j, jong",
            "k, kong",
            "l, long",
            "m, mong",
            "n, nong",
            "p, pong",
            "q, qong",
            "r, rong",
            "s, song",
            "t, tong",
            "v, vong",
            "w, wong",
            "x, xong",
            "y, yong",
            "z, zong"
    })
    void testConsonantTranslation(String input, String expected) {
        OngTranslator.TranslationResult r = translator.translate(input);
        assertWordEquals(expected, r, 0);
    }

    // ──────────────────────────────────────────────────────────────
    // Multi-word input
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("'Park Dog' produces two translated words")
    void testMultiWord() {
        OngTranslator.TranslationResult r = translator.translate("Park Dog");
        assertEquals(2, r.words().size());
        assertEquals("pong ā rong kong", r.words().get(0));
        assertEquals("dong ō gong",      r.words().get(1));
    }

    @Test
    @DisplayName("Case-insensitive — 'HELLO' same as 'hello'")
    void testCaseInsensitive() {
        String upper = translator.translate("HELLO").words().get(0);
        String lower = translator.translate("hello").words().get(0);
        assertEquals(upper, lower);
    }

    @Test
    @DisplayName("Punctuation inside a word is silently dropped")
    void testPunctuationDropped() {
        OngTranslator.TranslationResult r = translator.translate("dog!");
        assertWordEquals("dong ō gong", r, 0);
    }

    @Test
    @DisplayName("Original text is preserved in result")
    void testOriginalPreserved() {
        String input = "Hello World";
        OngTranslator.TranslationResult r = translator.translate(input);
        assertEquals(input, r.original());
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    private void assertWordEquals(String expected, OngTranslator.TranslationResult result, int wordIndex) {
        List<String> words = result.words();
        assertFalse(words.isEmpty(), "Expected at least one translated word");
        assertTrue(wordIndex < words.size(),
                "Word index " + wordIndex + " out of bounds (size=" + words.size() + ")");
        assertEquals(expected, words.get(wordIndex));
    }
}
