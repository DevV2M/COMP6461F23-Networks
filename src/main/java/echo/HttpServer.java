/**
 * COMP 6461 - Computer Networks and Protocols
 * Lab Assignment # 2
 * Group Members:
 * Vithu Maheswaran - 27052715
 * Shafiq Imtiaz - 40159305
 */

package echo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpServer {
    private static final String commandPattern = "httpfs\\s+(-v)?\\s*(-p\\s\\d+)?\\s*(-d\\s\\S+)?";
    private static final Pattern commandRegex = Pattern.compile(commandPattern);
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private static String serverDirectory;
    private static Set<Thread> threadSet = new HashSet<>();
    private static int currentClientCount = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(">> ");
            String command = sc.nextLine().trim();
            runCommand(command);
        }
    }

    public static void runCommand(String curlCommand) {
        Matcher commnandMatcher = commandRegex.matcher(curlCommand);
        if (commnandMatcher.find()) {
            String verboseFlag = commnandMatcher.group(1) != null ? commnandMatcher.group(1).split("\\s")[1] : null;
            String portFlag = commnandMatcher.group(2) != null ? commnandMatcher.group(2).split("\\s")[1] : null;
            String dirFlag = commnandMatcher.group(3) != null ? commnandMatcher.group(3).split("\\s")[1] : null;

            boolean isVerbose = verboseFlag != null;
            int port = (portFlag != null) ? Integer.parseInt(portFlag.trim()) : 8080;
            serverDirectory = (dirFlag != null) ? dirFlag.trim() : null;

//            System.out.println("-v " + isVerbose + " -p " + port + " -d " + serverDirectory);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server is listening on port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    currentClientCount = clientCount.incrementAndGet();
                    new Thread(() -> {
                        Thread.currentThread().setName("Client " + currentClientCount);
                        handleRequest(clientSocket, verboseFlag != null);
                    }).start();
                    System.out.println("Running clients: " + clientCount.get());
//                    System.out.println("Client threads: ");
//                    printRunningClients();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Command not matched");
        }
    }

    public static void printRunningClients() {
        threadSet = Thread.getAllStackTraces().keySet();
        threadSet.stream()
                .filter(thread -> thread.getName().startsWith("Client"))
                .forEach(thread -> System.out.println(thread.getName()));
    }

    private static void handleRequest(Socket clientSocket, boolean verbose) {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String requestLine = reader.readLine();
            if (requestLine != null) {
                String[] requestTokens = requestLine.split(" ");
                if (requestTokens.length == 3 && requestTokens[0].equals("GET")) {
                    String requestedPath = requestTokens[1];
                    String acceptHeader = getAcceptHeader(reader);

                    if (requestedPath.endsWith("/")) {
                        System.out.println("Path: " + requestedPath);
                        List<String> fileList = listFilesAndDirectories(requestedPath);
                        String response = generateResponse(fileList, acceptHeader);
                        sendHttpResponse(out, response);
                    } else if (requestedPath.startsWith("/")) {
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
                        if (checkIfMultipartFormData(headers)) {
                            String boundary = extractBoundaryFromHeader(headers);
                            bodyContent = extractBodyContent(boundary, receivedBody);
                        } else {
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
                    if (line.compareTo("--") == 0) continue;
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

    // TODO: NEED TO FIX TO READ AND WRITE
    private static boolean createOrUpdateFile(String filePath, String content, String overwriteOption) throws IOException {
        if ("false".equalsIgnoreCase(overwriteOption) && Files.exists(Paths.get(filePath))) {
            return false;
        }

        // Read the content from the request body and write it to the file
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw);
             BufferedReader reader = new BufferedReader(new StringReader(content))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
        return true;
    }

    private static List<String> listFilesAndDirectories(String currentPath) {
        String currentDirectory = System.getProperty("user.dir");

        System.out.println("Dir:" + currentDirectory);
        File folder = new File(currentDirectory + currentPath);
//        File folder = new File(serverDirectoryPath + currentPath);

        List<String> fileList = new ArrayList<>();
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(folder.getAbsolutePath()))) {
                for (Path path : stream) {
                    if (Files.isRegularFile(path) || Files.isDirectory(path)) {
                        fileList.add(path.getFileName().toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        StringBuilder listOfFiles = new StringBuilder();
        String extension = "";
        if (acceptHeader != null) {
            switch (acceptHeader) {
                case "json":
                    extension = ".json";
                    break;
                case "text/plain":
                    extension = ".txt";
                    break;
                case "xml":
                    extension = ".xml";
                    break;
                case "html":
                    extension = ".html";
                    break;
                default:
                    extension = "";
                    // code block
            }
        }
        for (String file : fileList) {
            if (extension == "") {
                listOfFiles.append(file);
                listOfFiles.append("\n");
            } else if (file.endsWith(extension)) {
                listOfFiles.append(file);
                listOfFiles.append("\n");
            }
        }
        return listOfFiles.toString();
    }

    private static String generateResponse(String content, String contentType) {
        return content;
    }

    private static void sendHttpResponse(OutputStream out, String response) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/plain\r\n"
                + "Content-Length: " + response.length() + "\r\n"
                + "\r\n"
                + response;
        out.write(httpResponse.getBytes());
    }

    private static void sendForbiddenResponse(OutputStream out) throws IOException {
        String forbiddenResponse = "HTTP/1.1 403 Forbidden\r\n\r\n";
        out.write(forbiddenResponse.getBytes());
    }

    private static void sendNotFoundResponse(OutputStream out) throws IOException {
        String notFoundResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        out.write(notFoundResponse.getBytes());
    }

    private static void sendCreatedResponse(OutputStream out) throws IOException {
        String createdResponse = "HTTP/1.1 201 Created\r\n\r\n";
        out.write(createdResponse.getBytes());
    }
}
