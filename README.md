# 2048

From a terminal, use JDK 24 or later and Maven:

```sh
mvn compile javafx:run
```

Use ↑ and ↓ to select Terminal or GUI, then press Enter. Use the same keys
to choose LXM (the default) or Classic (Java Random). Both give each empty
square an equal chance and spawn a 2 with 90% probability or a 4 with 10%.

Use the arrow keys to move the tiles. Press Esc to exit terminal mode,
or close the window to exit GUI mode. The terminal board fits in 31 × 13
characters and uses larger tiles in taller windows. GUI mode needs a desktop
session.

In IntelliJ IDEA, import `pom.xml` as a Maven project. Run the command above
in IntelliJ's Terminal tab for the interactive prompt.
