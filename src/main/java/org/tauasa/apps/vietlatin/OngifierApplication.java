package org.tauasa.apps.vietlatin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

/**
 * Entry point for the Ongifier application.
 *
 * <p>Run with:
 * <pre>
 *   export ELEVEN_LABS_API_KEY=your_key_here
 *   mvn spring-boot:run
 * </pre>
 *
 * Or pass text directly as a program argument:
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.arguments="Hello World"
 * </pre>
 * </p>
 */
@SpringBootApplication
public class OngifierApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OngifierApplication.class);

    private final OngifierService ongifierService;

    public OngifierApplication(OngifierService ongifierService) {
        this.ongifierService = ongifierService;
    }

    public static void main(String[] args) {
        SpringApplication.run(OngifierApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // If text was passed as CLI arguments, process once and exit.
        if (args.length > 0) {
            String input = String.join(" ", args);
            log.info("Processing CLI input: \"{}\"", input);
            ongifierService.process(input);
            return;
        }

        // Otherwise, start interactive REPL.
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          W E L C O M E  T O             ║");
        System.out.println("║           O N G I F I E R               ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Type any English text and hear it in Ong!");
        System.out.println("  (type 'quit' or 'exit' to stop)");
        System.out.println();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter text: ");
                String input = scanner.nextLine().trim();

                if (input.isBlank()) {
                    System.out.println("Please enter some text.");
                    continue;
                }

                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye! dong ō gong!");
                    break;
                }

                try {
                    ongifierService.process(input);
                } catch (Exception e) {
                    log.error("Error processing input: {}", e.getMessage(), e);
                    System.out.println("Something went wrong — check your API key and try again.");
                }
            }
        }
    }
}
