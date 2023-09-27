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
//      urlStr = "http://httpbin.org/status/418";
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

        Socket socket = new Socket(host,port);

        // Define headers
        Map<String, String> headers = new HashMap<>();


//        get(pathWithQuery, host, socket, headers);
//
        JSONObject data = new JSONObject();
//
        data.put("Vithu","Best");
//        String data = "Hello";
//
//        post(data,pathWithQuery,socket, headers);

      postFile("./text.txt",socket, headers, false);


    }

    private static void get(String path, Socket socket, Map<String, String> headers) {
        //from www.java2s.com
        try {
            StringBuilder request = new StringBuilder()
                    .append(String.format("GET %s HTTP/1.0\r\n", path))
                    .append(String.format("Host: %s\r\n",socket.getInetAddress().getHostName()));

            // Add custom headers to the request
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            request.append("\r\n");

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writeToSocket(wr, request.toString());
            String[] response = readFromSocket(rd);
            System.out.println(response[0]);

            wr.close();
            rd.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        private static void post(String data, String path, Socket socket, Map<String, String> headers) throws IOException {

            StringBuilder request = new StringBuilder()
                    .append(String.format("POST %s HTTP/1.0\r\n",path))
                    .append(String.format("Content-Length: %s\r\n",data.length()));

            // Add custom headers to the request
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            request.append("\r\n");
            request.append(data);

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writeToSocket(wr, request.toString());
            String[] response = readFromSocket(rd);
            System.out.println(response[0]);

            wr.close();
            rd.close();

            wr.close();
            rd.close();
        }

    private static void post(JSONObject data, String path, Socket socket, Map<String, String> headers) throws IOException {

        StringBuilder request = new StringBuilder()
                .append(String.format("POST %s HTTP/1.0\r\n",path))
                .append(String.format("Content-Length: %s\r\n",data.toJSONString().length()))
                .append("Content-Type: application/json\r\n");

        // Add custom headers to the request
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        request.append("\r\n")
                .append(data.toJSONString());

        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));

        writeToSocket(wr,request.toString());
        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String[] response = readFromSocket(rd);

        System.out.println(response[0]);
        wr.close();
        rd.close();
    }

        public static void postFile(String filePath, Socket socket, Map<String, String> headers, Boolean verbose) {

            try (OutputStream os = socket.getOutputStream()) {

                String requestBody = getRequestBodyForPostFile(filePath);
                String requestHeader = getRequestHeaderForPostFile(socket,requestBody.getBytes().length,headers);

                // Post the request with file content
                os.write(requestHeader.getBytes());
                os.write(requestBody.getBytes());
                os.flush();

                // Read and print the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String[] response = readFromSocket(rd);

                if (verbose) {
                    for (String str : response) {
                        System.out.println(str);
                    }
                } else {
                    System.out.println(response[0]);
                }

                os.close();
                rd.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static String getRequestBodyForPostFile(String filePath) throws IOException {

        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[8192];
        int bytesRead;
        StringBuilder data = new StringBuilder();

        while ((bytesRead = fis.read(buffer)) != -1) {
            data.append(new String(buffer, 0, bytesRead));
        }

        // Data as a single string
        String allData = data.toString();

        String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());

        // Write the file data as a part of the multipart request
        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("--").append(boundary).append("\r\n");
        requestBodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"text.txt\"\r\n");
        requestBodyBuilder.append("Content-Type: application/octet-stream\r\n");
        requestBodyBuilder.append("\r\n");
        requestBodyBuilder.append(allData);
        requestBodyBuilder.append("\r\n").append("--").append(boundary).append("--\r\n");
        return requestBodyBuilder.toString();
    }
    public static String getRequestHeaderForPostFile(Socket socket, int size, Map<String, String> headers){

        String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
        StringBuilder requestHeader = new StringBuilder();
        requestHeader.append("POST /post HTTP/1.0\r\n")
                .append(String.format("Host: %s\r\n",socket.getInetAddress().getHostName()));
        // Add custom headers to the request
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestHeader.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
//                requestHeader.append("\r\n");
        requestHeader.append(String.format("Content-Length: %d\r\n", size))
                .append(String.format("Content-Type: multipart/form-data; boundary=%s\r\n\r\n",boundary));

        return requestHeader.toString();
    }

    public static void writeToSocket(BufferedWriter wr, String request) throws IOException {
        wr.write(request.toString());
        wr.flush();
    }

    public static String[] readFromSocket(BufferedReader rd) throws IOException {
        String line;
        StringBuilder response = new StringBuilder();
        String[] responseHeaderAndBody = new String[2];
        while ((line = rd.readLine()) != null) {
            if(line.trim().isEmpty()){
                responseHeaderAndBody[0] = response.toString();
                response = new StringBuilder();
                continue;
            }
            response.append(line.trim()).append("\r\n");
        }
        responseHeaderAndBody[1] = response.toString();
        return responseHeaderAndBody;
    }
}
