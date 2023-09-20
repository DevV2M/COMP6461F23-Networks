package ca.concordia.echo;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Arrays.asList;

public class BlockingHttpClient {

    private static void readFully(SocketChannel socket, ByteBuffer buf, int size) throws IOException {
        while (buf.position() < size) {
            int n = socket.read(buf);
            if (n == -1) {
                throw new EOFException("Premature end of stream");
            }
        }
    }


    private static void sendHttpRequest(SocketChannel socket) throws IOException {
        Charset utf8 = StandardCharsets.UTF_8;

        // Construct the HTTP GET request
        String request = "GET /status/418 HTTP/1.0\r\nHost: httpbin.org\r\n\r\n";
        ByteBuffer buf = utf8.encode(request);
        int n = socket.write(buf);
        buf.clear();

        // Receive the response
        readFully(socket, buf, n);
        buf.flip();
        System.out.println("Response:\n" + utf8.decode(buf));
    }

    private static void runHttpClient(SocketAddress endpoint) throws IOException {
        try (SocketChannel socket = SocketChannel.open()) {
            socket.connect(endpoint);
            System.out.println("Sending HTTP GET request to httpbin.org...");
            sendHttpRequest(socket);
        }
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("host", "h"), "HTTP server hostname")
                .withOptionalArg()
                .defaultsTo("httpbin.org");

        parser.acceptsAll(asList("port", "p"), "HTTP server listening port")
                .withOptionalArg()
                .defaultsTo("80");

        OptionSet opts = parser.parse(args);

        String host = (String) opts.valueOf("host");
        int port = Integer.parseInt((String) opts.valueOf("port"));

        SocketAddress endpoint = new InetSocketAddress(host, port);
        runHttpClient(endpoint);
    }
}
