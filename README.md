# 🎤 Vīētlātīn Ōngīfīēr

Translates English text into the **Ong Dialect** of the made-up language **Vietlatin** and reads it aloud via Text-to-Speech backed by [ElevenLabs](https://elevenlabs.io) or [OpenAI](https://platform.openai.com/api-keys), powered by **Spring AI**.

Although it's a made-up/pretend language, my wife and I use it daily for certain keywords that get our dogs all amped up. Since we can't say words like "park," "ride," "cookie," "treat," etc, we spell it out in Vietlatin.

---

## How it works

Each word is split into characters. Every character is transformed:

| Character type | Rule | Example |
|---|---|---|
| Consonant | append `ong` | `p` → `pong` |
| Vowel A | replace | `a` → `ā` |
| Vowel E | replace | `e` → `ē` |
| Vowel I | replace | `i` → `ī` |
| Vowel O | replace | `o` → `ō` |
| Vowel U | replace | `u` → `ū` |

### Examples
```
Park    → pong ā rong kong
Dog     → dong ō gong
Cookie  → cong ō ō kong ī ē
Treat   → tong rong ē ā tong
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.9+ |
| ElevenLabs API key | [Get one free](https://elevenlabs.io) |
| OpenAI API key | [Get one free](https://platform.openai.com/api-keys) |

---

## Quick Start with ElevenLabs

### 1. Get an ElevenLabs or OpenAI API key

Sign up at <https://elevenlabs.io> — you get **10,000 free credits/month**.

### 2. Set your API key

```bash
export ELEVEN_LABS_API_KEY=your_elevenlabs_key_here
```
**or**
```bash
export OPENAI_API_KEY=your_openai_key_here
```

### 3. (Optional) Customise the voice

Edit `src/main/resources/application.properties`:

```properties
# Browse voices at https://elevenlabs.io/voice-library
spring.ai.elevenlabs.tts.voice-id=pNInz6obpgDQGcFmaJgB   # default: Adam
spring.ai.elevenlabs.tts.model=eleven_turbo_v2_5
spring.ai.elevenlabs.tts.output-format=mp3_44100_128
```

### 4. Run interactively

```bash
mvn spring-boot:run
```

You'll see:

```
╔══════════════════════════════════════════╗
║          W E L C O M E  T O              ║
║          V I E T L A T I N               ║
╚══════════════════════════════════════════╝
  Type any English text and hear it in Ong!
  (type 'quit' or 'exit' to stop)

Enter text: Park
═══════════════════════════════════════════
  Original  : Park
  Ong form  : pong ā rong kong
═══════════════════════════════════════════
```

### 5. Run with a single argument

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="Hello World"
```

---

## Running the tests

```bash
mvn test
```

The unit tests cover all 26 consonants, all 5 vowels (upper and lower case),
multi-word inputs, and the three spec examples. No API key required.

---

## Audio output

The generated MP3 is saved as `output.mp3` in the working directory.
If your JDK includes an MP3 codec (e.g. via `mp3plugin`), playback starts
automatically. Otherwise, open the file with your OS media player:

```bash
open output.mp3        # macOS
xdg-open output.mp3    # Linux
start output.mp3       # Windows
```

---

## Project structure

```
vietlatin/
├── pom.xml
├── README.md
├── output.mp3
└── src/
    ├── main/
    │   ├── java/org/tauasa/apps/vietlatin/
    │   │   ├── OngifierApplication.java   ← Entry point + CLI REPL
    │   │   ├── OngTranslator.java         ← Pure translation logic
    │   │   ├── OngifierService.java       ← Orchestrates translate → TTS → play
    │   │   └── AudioPlayerService.java    ← Saves MP3 + attempts playback
    │   └── resources/
    │       └── application.properties     ← API key + voice config
    └── test/
        └── java/org/tauasa/apps/vietlatin/
            └── OngTranslatorTest.java     ← 30+ unit tests, no API key needed
```
