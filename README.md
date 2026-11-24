# 2048

From a terminal, use JDK 24 or later and Maven:

```sh
mvn compile javafx:run
```

At the prompt, enter `terminal` for the Lanterna version or `gui` for the
JavaFX window. Use the arrow keys to move the tiles. Press Esc to exit terminal
mode, or close the window to exit GUI mode. The terminal board fits in 31 × 13
characters and uses larger tiles in taller windows. GUI mode needs a desktop
session.

In IntelliJ IDEA, import `pom.xml` as a Maven project. Run the command above
in IntelliJ's Terminal tab for the interactive prompt.
