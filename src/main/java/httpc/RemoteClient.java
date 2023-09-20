package ca.concordia.httpc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

import static java.util.Arrays.asList;

public class RemoteClient {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String[] req = sc.nextLine().split(" ");
        String type = req[0].toUpperCase();
        String host = req[1];
        String path = req[2];
        int port = 80;

//        OptionParser parser = new OptionParser();
//        parser.acceptsAll(asList("host", "h"), "HTTP server hostname")
//                .withOptionalArg()
//                .defaultsTo(host);
//
//        parser.acceptsAll(asList("port", "p"), "HTTP server listening port")
//                .withOptionalArg()
//                .defaultsTo(port);
//
//        OptionSet opts = parser.parse(args);
//
//        String host = (String) opts.valueOf("host");
//        int port = Integer.parseInt((String) opts.valueOf("port"));
//
//        SocketAddress endpoint = new InetSocketAddress(host, port);

        sendHttp(host, port, type, path);

    }

    public static void sendHttp(String host, int port, String type, String path) {
        String request = "";

        if (type.equals("GET")) {
//            get httpbin.org /status/418
            request = type + " " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
                    "\r\n";

        } else if (type.equals("POST")) {
//            https://postman-echo.com/post
//            String data = "foo1=bar1&foo2=bar2";
//            String data = """
//                    {
//                        "test": "value"
//                    }""";

//            post httpbin.org /status/418
            request = type + " " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + "\r\n" +
//                    "Content-Type: application/x-www-form-urlencoded\r\n" +
//                    "Content-Length: " + data.length() + "\r\n" +
                    "\r\n";
//                    + data;
        }
            try {
            // Create a socket and connect to the server
            Socket socket = new Socket(host, port);

            // Create output stream to send the request
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request.getBytes());
            outputStream.flush();

            // Create input stream to read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Print the response
            System.out.println("Response Data:");
            System.out.println(response.toString());

            // Close the streams and socket
            outputStream.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
