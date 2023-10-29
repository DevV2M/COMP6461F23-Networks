package echo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpServer {

    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String requestLine = reader.readLine();
            if (requestLine != null) {
                String[] requestTokens = requestLine.split(" ");
                if (requestTokens.length == 3 && requestTokens[0].equals("GET")) {
                    String requestedPath = requestTokens[1];
                    String acceptHeader = getAcceptHeader(reader);
                    if ("/".equals(requestedPath)) {
                        List<String> fileList = listFilesInDataDirectory();
                        String response = generateResponse(fileList, acceptHeader);
                        sendHttpResponse(out, response);
                    } else if (requestedPath.startsWith("/")) {
                        // Requested file path
                        String filePath = requestedPath.substring(1);
                        System.out.println(filePath);
                        if (Files.exists(Paths.get(filePath))) {
                            String fileContent = getFileContent(filePath);
                            String response = generateResponse(fileContent, acceptHeader);
                            sendHttpResponse(out, response);
                        } else {
                            sendNotFoundResponse(out);
                        }
                    } else {
                        sendNotFoundResponse(out);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> listFilesInDataDirectory() {
        // Replace with the actual logic to list files in your data directory
        List<String> fileList = new ArrayList<>();
        fileList.add("file1.txt");
        fileList.add("file2.txt");
        return fileList;
    }

    private static String getFileContent(String filePath) throws IOException {
        // Read and return the content of the file
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static String getAcceptHeader(BufferedReader reader) throws IOException {
        String acceptHeader = "";
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Accept: ")) {
                acceptHeader = line.substring("Accept: ".length());
                break;
            }
        }
        return acceptHeader;
    }

    private static String generateResponse(List<String> fileList, String acceptHeader) {
//        if (acceptHeader.contains("application/json")) {
//            // Generate JSON response
//            // You would need to implement JSON serialization here
//            return "JSON response: " + fileList.toString();
//        } else if (acceptHeader.contains("application/xml")) {
//            // Generate XML response
//            // You would need to implement XML serialization here
//            return "XML response: " + fileList.toString();
//        } else if (acceptHeader.contains("text/plain")) {
//            // Generate plain text response
//            return "Text response: " + String.join("\n", fileList);
//        } else if (acceptHeader.contains("text/html")) {
//            // Generate HTML response
//            // You would need to implement HTML response generation here
//            return "HTML response: " + fileList.toString();
//        } else {
//            // Default to JSON if no specific format is requested
//            return "JSON response: " + fileList.toString();
//        }
        return fileList.toString();
    }

    private static String generateResponse(String content, String acceptHeader) {
        if (acceptHeader.contains("application/json")) {
            // Generate JSON response
            // You would need to implement JSON serialization here
            return "JSON response: " + content;
        } else if (acceptHeader.contains("application/xml")) {
            // Generate XML response
            // You would need to implement XML serialization here
            return "XML response: " + content;
        } else if (acceptHeader.contains("text/plain")) {
            // Generate plain text response
            return content;
        } else if (acceptHeader.contains("text/html")) {
            // Generate HTML response
            // You would need to implement HTML response generation here
            return "HTML response: " + content;
        } else {
            // Default to JSON if no specific format is requested
            return "JSON response: " + content;
        }
    }

    private static void sendHttpResponse(OutputStream out, String response) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/plain\r\n"
                + "Content-Length: " + response.length() + "\r\n"
                + "\r\n"
                + response;
        out.write(httpResponse.getBytes());
    }

    private static void sendNotFoundResponse(OutputStream out) throws IOException {
        String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        out.write(notFoundResponse.getBytes());
    }
}
