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
//        String urlStr = "http://httpbin.org/get?course=networking&assignment=1";
//        urlStr = "http://httpbin.org/status/418";
        String urlStr = "http://httpbin.org/post";
        java.net.URL url = new URL(urlStr);

        int port = 0;
        if ((port = url.getPort()) == -1) port = url.getDefaultPort();

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

//        post(pathWithQuery, "httpbin.org",endpoint, headers);

        postFile("./text.txt",host,port);


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
//            wr.write("Content-Length: " + data + "\r\n"); // for plain text
            wr.write("Content-Length: " + data.toJSONString().length() + "\r\n");
//            wr.write("Content-Type: application/json\r\n");
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

        public static void postFile(String filePath, String Host, int Port){
            String serviceUrl = "http://httpbin.org/post";

            String serviceHost = "httpbin.org";
            int servicePort = 80;

            try (Socket socket = new Socket(serviceHost, servicePort);
                 OutputStream os = socket.getOutputStream();
                 FileInputStream fis = new FileInputStream(filePath)) {

                String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());


                byte[] buffer = new byte[8192];
                int bytesRead;
                StringBuilder data = new StringBuilder();

                while ((bytesRead = fis.read(buffer)) != -1) {
                    data.append(new String(buffer, 0, bytesRead));
                }

                // Data as a single string
                String allData = data.toString();


                // Write the file data as a part of the multipart request
                StringBuilder requestBodyBuilder = new StringBuilder();
                requestBodyBuilder.append("--").append(boundary).append("\r\n");
                requestBodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"text.txt\"\r\n");
                requestBodyBuilder.append("Content-Type: application/octet-stream\r\n");
                requestBodyBuilder.append("\r\n");
                requestBodyBuilder.append(allData);
                requestBodyBuilder.append("\r\n").append("--").append(boundary).append("--\r\n");
                String requestBody = new String(requestBodyBuilder.toString());

                int size = requestBody.getBytes().length;

                String requestHeaders = "POST /post HTTP/1.0\r\n" +
                        "Host: " + serviceHost + "\r\n" +
                        "Content-Length: " + size + "\r\n" +
                        "Content-Type: multipart/form-data; boundary=" + boundary + "\r\n\r\n";

                os.write(requestHeaders.getBytes());
                os.write(requestBody.getBytes());
                os.flush();

                // Read and print the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                }

                rd.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//    public static void postFile(String filePath, String serviceHost, int servicePort){
//        String serviceUrl = "http://httpbin.org/post";
//
//        try {
//            HttpURLConnection connection = (HttpURLConnection) new URL(serviceUrl).openConnection();
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//
//            // Set the Content-Type header for file upload
//            String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//            // Open the file as an InputStream
//            try (OutputStream os = connection.getOutputStream();
//                 FileInputStream fis = new FileInputStream(filePath)) {
//
//                // Send the file header
//                String fileHeader = "--" + boundary + "\r\n" +
//                        "Content-Disposition: form-data; name=\"file\"; filename=\"temp.txt\"\r\n" +
//                        "Content-Type: application/octet-stream\r\n\r\n";
//                os.write(fileHeader.getBytes());
//
//                // Send the file content
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = fis.read(buffer)) != -1) {
//                    os.write(buffer, 0, bytesRead);
//                }
//
//                // Send the closing boundary
//                String boundaryEnd = "\r\n--" + boundary + "--\r\n";
//                os.write(boundaryEnd.getBytes());
//            }
//
//            // Get and print the response
//            int responseCode = connection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                    String inputLine;
//                    while ((inputLine = in.readLine()) != null) {
//                        System.out.println(inputLine);
//                    }
//                }
//            } else {
//                System.err.println("HTTP Error: " + responseCode);
//            }
//
//            connection.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
