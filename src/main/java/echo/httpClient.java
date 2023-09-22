package echo;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;

public class httpClient {
    public static void main(String[] args) throws IOException {
        String urlStr = "http://httpbin.org/get?course=networking&assignment=1";
//        urlStr = "http://httpbin.org/status/418";
        java.net.URL url = new URL(urlStr);

        int port = url.getPort();
        if (port == -1) port = url.getDefaultPort();

        String host = url.getHost();

        URI uri = null;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String pathWithQuery = uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
        SocketAddress endpoint = new InetSocketAddress(host, port);

        // Define headers
        Map<String, String> headers = new HashMap<>();
//        get(pathWithQuery, endpoint, headers);
        post(pathWithQuery, "httpbin.org",endpoint, headers);
    }

    private static void get(String path, SocketAddress address, Map<String, String> headers) {
        //from www.java2s.com
        try {
            StringBuilder request = new StringBuilder()
                    .append(String.format("GET %s HTTP/1.0\r\n", path))
                    .append("Host: httpbin.org\r\n");

            // Add custom headers to the request
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            request.append("\r\n");

            SocketChannel channel = SocketChannel.open(address);
            System.out.println(String.format("get: request >> [%s]", request.toString()));
            channel.write(ByteBuffer.wrap(request.toString().getBytes()));
            ByteBuffer buf = ByteBuffer.allocate(4 * 1024);
            while (channel.read(buf) > -1);
            String response = new String(buf.array());
            System.out.println(String.format("get: << Response: %s", response));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        private static void post(String path1, String host, SocketAddress address, Map<String, String> headers) throws IOException {
            JSONObject data = new JSONObject();

            data.put("name", "Pankaj Kumar");
            data.put("age", 32);
    //        String data = "Vithu";

            Socket socket = new Socket("httpbin.org", 80);

            String path = "/post";
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            wr.write("POST " + path + " HTTP/1.0\r\n");
    //        wr.write("Content-Length: " + data + "\r\n"); // for plain text
            wr.write("Content-Length: " + data.toJSONString().length() + "\r\n");
            wr.write("Content-Type: application/json\r\n");
            wr.write("\r\n");

            wr.write(data.toJSONString());
            wr.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }
            wr.close();
            rd.close();
        }
}
