package technology;

import java.io.IOException;

public interface Input {

    public KeyPress pollKeyboard() throws IOException;
}
