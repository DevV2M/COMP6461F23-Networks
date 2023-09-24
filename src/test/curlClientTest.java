import echo.CurlClient;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class curlClientTest {

    @Test
    public void testInvalidCommand() {
        String input = "httpc invalidCommand\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        new CurlClient();

        String output = out.toString();
        assertEquals("Invalid command (httpc)\n", output);
    }
}
