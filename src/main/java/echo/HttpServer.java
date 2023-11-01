package echo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                } else if (requestTokens.length == 3 && requestTokens[0].equals("POST")) {
                    String requestedPath = requestTokens[1];
                    if (requestedPath.startsWith("/")) {
                        String delimiter = "\r\n\r\n";
                        int bytesRead;
                        StringBuilder receivedData = new StringBuilder();
                        char[] buffer = new char[1024]; // Adjust buffer size as needed
                        String receivedHeader = null;
                        String receivedBody = null;
                        while ((bytesRead = reader.read(buffer)) != -1) {
                            receivedData.append(buffer, 0, bytesRead);
                            // Check if the delimiter has been received
                            int delimiterIndex = receivedData.indexOf(delimiter);
                            if (delimiterIndex >= 0) {
                                // Split data at the delimiter
                                receivedHeader = receivedData.substring(0, delimiterIndex);
                                receivedBody = receivedData.substring(delimiterIndex + delimiter.length());
                                break;
                            }
                        }
                        List<String> headers = getRequestHeaders(receivedHeader);
                        String bodyContent = null;
                        if(checkIfMultipartFormData(headers)){
                            String boundary = extractBoundaryFromHeader(headers);
                            bodyContent = extractBodyContent(boundary, receivedBody);
                        } else{
                            bodyContent = receivedBody;
                        }

                        String overwriteOption = getOverwriteOption(headers);
                        // Requested file path
                        String postTofilePath = requestedPath.substring(1);

                        if (createOrUpdateFile(postTofilePath, bodyContent, overwriteOption)) {
                            sendCreatedResponse(out);
                        } else {
                            sendForbiddenResponse(out);
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

    private static boolean checkIfMultipartFormData(List<String> headers) {
        Pattern pattern = Pattern.compile("Content-Type:.*?multipart/form-data.*?");
        for (String header : headers) {
            if (header.startsWith("Content-Type")) {
                Matcher matcher = pattern.matcher(header);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;

    }

    private static String extractBodyContent(String boundary, String requestBody) throws IOException {

        if (boundary != null) {
            // Construct the regular expression pattern to match the content between the boundary lines
            String regex = Pattern.quote(boundary) + "(.*?)" + Pattern.quote(boundary);
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

            // Use the pattern to match and extract the POST command body
            Matcher matcher = pattern.matcher(requestBody);

            if (matcher.find()) {
                String postBodyContentWithHeaders = matcher.group(1).trim();
                BufferedReader reader = new BufferedReader(new StringReader(postBodyContentWithHeaders));

                String line;
                StringBuilder bodyContent = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.isEmpty()) ;
                while ((line = reader.readLine()) != null) {
                    if(line.compareTo("--") == 0) continue;
                    bodyContent.append(line).append("\n");
                }
                String content = bodyContent.toString().trim();
                return content;
            }
        } else {
            System.out.println("Boundary not found in the request header.");
        }
        return null;
    }

    private static String extractBoundaryFromHeader(List<String> headers) {
        // Define the regular expression pattern to extract the boundary parameter from the Content-Type header
        Pattern pattern = Pattern.compile("Content-Type:.*?boundary=([\\w\\-]+)");
        for (String header : headers) {
            System.out.println(header);
            if (header.startsWith("Content-Type")) {
                Matcher matcher = pattern.matcher(header);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    private static String extractFilePathFromBody(List<String> headers) {
        // Define the regular expression pattern to extract the boundary parameter from the Content-Type header
        Pattern pattern = Pattern.compile("Content-Type:.*?boundary=([\\w\\-]+)");
        for (String header : headers) {
            System.out.println(header);
            if (header.startsWith("Content-Type")) {
                Matcher matcher = pattern.matcher(header);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    private static String getOverwriteOption(List<String> headers) throws IOException {
        String overwriteOption = "true";

        for (String header : headers) {
            if (header.startsWith("Overwrite: ")) {
                overwriteOption = header.substring("Overwrite: ".length());
                break;
            }
        }
        return overwriteOption;
    }

    private static List<String> getRequestHeaders(String receivedHeader) throws IOException {
        List<String> headers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(receivedHeader))) {
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headers.add(line);
            }
        }
        return headers;
    }

    // NEED TO FIX TO READ AND WRITE
    private static boolean createOrUpdateFile(String filePath, String content, String overwriteOption) throws IOException {
        if ("false".equalsIgnoreCase(overwriteOption) && Files.exists(Paths.get(filePath))) {
            return false;
        }

        // Read the content from the request body and write it to the file
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
             BufferedWriter writer = new BufferedWriter(osw);
             BufferedReader reader = new BufferedReader(new StringReader(content))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine(); //TODO: Check if this creates an additional new line
            }
        }
        return true;
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

    private static void sendCreatedResponse(OutputStream out) throws IOException {
        String createdResponse = "HTTP/1.1 201 Created\r\n\r\n";
        out.write(createdResponse.getBytes());
    }

    private static void sendForbiddenResponse(OutputStream out) throws IOException {
        String forbiddenResponse = "HTTP/1.1 403 Forbidden\r\n\r\n";
        out.write(forbiddenResponse.getBytes());
    }

}
