/**
 * COMP 6461 - Computer Networks and Protocols
 * Lab Assignment # 2
 * Group Members:
 * Vithu Maheswaran - 27052715
 * Shafiq Imtiaz - 40159305
 */

package echo.UDP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpServerLibrary {
    private static final String commandPattern = "httpfs\\s+(-v)?\\s*(-p\\s\\d+)?\\s*(-d\\s\\S+)?";
    private static final Pattern commandRegex = Pattern.compile(commandPattern);
    private static String serverDirectoryPath = System.getProperty("user.dir");
    private static List<Thread> threadList = new ArrayList<>();
    private static AtomicInteger currentClientCount = new AtomicInteger(0);

    public HttpServerLibrary() {
    }

//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        System.out.print(">> ");
//        String command = sc.nextLine().trim();
//        runCommand(command);
//    }

//    public static void runCommand(String curlCommand) {
//        Matcher commnandMatcher = commandRegex.matcher(curlCommand);
//        if (commnandMatcher.find()) {
//            String verboseFlag = commnandMatcher.group(1) != null ? commnandMatcher.group(1).split("\\s")[1] : null;
//            String portFlag = commnandMatcher.group(2) != null ? commnandMatcher.group(2).split("\\s")[1] : null;
//            String dirFlag = commnandMatcher.group(3) != null ? commnandMatcher.group(3).split("\\s")[1] : null;
//
//            boolean isVerbose = verboseFlag != null;
//            int port = (portFlag != null) ? Integer.parseInt(portFlag.trim()) : 8080;
//            serverDirectoryPath = (dirFlag != null) ? dirFlag.trim() : System.getProperty("user.dir");
//            String dirPath = new File(serverDirectoryPath).getAbsolutePath();
//
//            try (ServerSocket serverSocket = new ServerSocket(port)) {
//                System.out.println("Server is listening on port " + port + " at directory " + dirPath);
//                while (true) {
//                    Socket clientSocket = serverSocket.accept();
//                    CountDownLatch latch = new CountDownLatch(1);
//                    currentClientCount.incrementAndGet();
//                    Thread clientThread = new Thread(() -> {
//                        Thread.currentThread().setName("Client " + currentClientCount);
//                        handleRequest(clientSocket, verboseFlag != null);
//                        latch.countDown();  // Signal that the thread has started
//                    });
//                    clientThread.start();
//                    try {
//                        latch.await();  // Wait until the thread has started
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    threadList.add(clientThread);
//                    System.out.println("Running clients: " + getClientThreads());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("Command not matched");
//        }
//    }


    public static List<String> getClientThreads() {
        List<String> clientThreads = new ArrayList<>();
        for (Thread thread : threadList) {
            if (thread.getName().startsWith("Client")) {
                clientThreads.add(thread.getName());
            }
        }
        return clientThreads;
    }

    public static boolean validPath(String reqPath) throws IOException {
        Path rootDirectory = Paths.get(serverDirectoryPath).toAbsolutePath().normalize();
        Path filePath = rootDirectory.resolve(reqPath).normalize();
        return filePath.startsWith(rootDirectory) ? true : false;
    }

    //    public static void handleRequest(Socket clientSocket, boolean verbose, String msg) {
    public static String handleRequest(String msg) throws IOException {
//        try (InputStream in = clientSocket.getInputStream();
//             OutputStream out = clientSocket.getOutputStream();
//             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
//        try {

//            String requestLine = reader.readLine();
//        System.out.println("MESSAGE: " + msg);
        String requestLine = msg;
        if (requestLine != null) {

            String[] requestLines = requestLine.split("\r\n");
            String[] requestTokens = requestLines[0].split(" ");
//            System.out.println("SIZE: " + requestTokens.length);
//            System.out.println("Value: " + requestTokens[0]);
//            System.out.println("Value: " + requestTokens[1]);
//            System.out.println("Value: " + requestTokens[2]);
//            System.out.println("Value: " + requestTokens[3]);
//                System.out.println("****HERE: " + requestTokens[1].substring(1));
//                if (!validPath(requestTokens[1].substring(1))) {
////                    sendForbiddenResponse(out);
//                    return "Forbidden";
//                }
            if (requestTokens.length == 3 && requestTokens[0].equals("GET")) {
                String requestedPath = requestTokens[1];
//                    String acceptHeader = getAcceptHeader(reader).toLowerCase();
                String acceptHeader = "";
//                System.out.println("Requested Path: " + requestedPath);
                if (requestedPath.endsWith("/")) {
                    try {
                        List<String> fileList = listFilesAndDirectories(requestedPath);
                        String response = generateResponse(fileList, acceptHeader);
                        UDPClient client = new UDPClient(response);
//                            sendHttpResponse(out, response);
                    } catch (IOException e) {
//                            sendNotFoundResponse(out);
                    }
                } else if (requestedPath.startsWith("/")) {
                    try {
//                        System.out.println("here");
                        String filePathWithFileName = getFileNameWithPath(requestedPath, acceptHeader);
//                        System.out.println("FILENAME: " + filePathWithFileName);
//                        String filePathWithFileName = requestedPath.substring(1);
                        if (Files.exists(Paths.get(filePathWithFileName))) {
                            String fileContent = getFileContent(filePathWithFileName);
                            String response = generateResponse(fileContent, acceptHeader);
                            return response;
//                                UDPClient client = new UDPClient(response);
//                                sendHttpResponse(out, response);
                        } else {
//                                sendNotFoundResponse(out);
                        }
                    } catch (IOException e) {
//                            sendNotFoundResponse(out);
                    }
                } else {
//                        sendNotFoundResponse(out);
                }
            } else if (requestTokens.length == 3 && requestTokens[0].equals("POST")) {
                String requestedPath = requestTokens[1];
                if (requestedPath.startsWith("/")) {
                    String delimiter = "\r\n\r\n";
                    StringBuilder receivedData = new StringBuilder();
//                    char[] buffer = new char[1024];
                    String receivedHeader = null;
                    String receivedBody = null;

                    // Convert the message string to a char array
                    char[] buffer = msg.toCharArray();
                    int bytesRead = buffer.length;

                    receivedData.append(buffer, 0, bytesRead);
//                    while ((bytesRead = reader.read(buffer)) != -1) {
//                        receivedData.append(buffer, 0, bytesRead);
//                        // Check if the delimiter has been received
//                        int delimiterIndex = receivedData.indexOf(delimiter);
//                        if (delimiterIndex >= 0) {
//                            // Split data at the delimiter
//                            receivedHeader = receivedData.substring(0, delimiterIndex);
//                            receivedBody = receivedData.substring(delimiterIndex + delimiter.length());
//                            break;
//                        }
//                    }
                    // Check if the delimiter has been received
                    int delimiterIndex = receivedData.indexOf(delimiter);
                    if (delimiterIndex >= 0) {
                        // Split data at the delimiter
                        receivedHeader = receivedData.substring(0, delimiterIndex);
                        receivedBody = receivedData.substring(delimiterIndex + delimiter.length());
                        // Do something with the header and body
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
                    String postTofilePath = serverDirectoryPath + requestedPath;

//                    System.out.println(postTofilePath);
//                    System.out.println(bodyContent);
//                    System.out.println(overwriteOption);

                    if (createOrUpdateFile(postTofilePath, bodyContent, overwriteOption)) {
//                        sendCreatedResponse(out);
                    } else {
//                        sendForbiddenResponse(out);
                    }
                } else {
//                    sendNotFoundResponse(out);
                }
            }
        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return "";
    }

    private static String getFileNameWithPath(String filePath, String acceptHeader) throws IOException {

        // Define a regular expression pattern to match the part before the last '/'
        Pattern pattern = Pattern.compile("(.*/)(.*)");
        Matcher matcher = pattern.matcher(filePath);
//        System.out.println("filepath: " + filePath);
        if (matcher.find()) {
            String path = matcher.group(1);
            String fileName = matcher.group(2);
//            System.out.println("path: " + path);
//            System.out.println("fileName: " + fileName);
            List<String> listOfFilesAndFolders = listFilesAndDirectories(path);
            String extension = resolveAcceptHeader(acceptHeader);
            if (extension != "") {
                for (String file : listOfFilesAndFolders) {
                    if (file.startsWith(fileName) && file.endsWith(extension)) {
                        return serverDirectoryPath + path + file;
                    }
                }
            } else {
                for (String file : listOfFilesAndFolders) {
                    if (file.startsWith(fileName + ".")) {
                        return serverDirectoryPath + path + file;
                    }
                }
            }
        } else {
            System.out.println("No match found.");
        }
        return "";
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

    private static synchronized boolean createOrUpdateFile(String filePath, String content, String overwriteOption) throws IOException {
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
        } catch (NullPointerException e) {
            System.out.println("Content: null");
        }
        return true;
    }

    private static List<String> listFilesAndDirectories(String currentPath) throws IOException {
        String currentDirectory = serverDirectoryPath;
        File folder = new File(currentDirectory + currentPath);

        List<String> fileList = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(folder.getAbsolutePath()));
        for (Path path : stream) {
            if (Files.isRegularFile(path) || Files.isDirectory(path)) {
                fileList.add(path.getFileName().toString());
            }
        }
        return fileList;
    }


    private static String getFileContent(String filePath) throws IOException {
        // Read and return the content of the file
        StringBuilder content = new StringBuilder();
        // Create a FileReader and BufferedReader to read the file
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
            content.append("\n");
        }

        // Close the resources when done
        bufferedReader.close();
        fileReader.close();
        return content.toString();
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
        String extension = resolveAcceptHeader(acceptHeader);

        for (String file : fileList) {
            if (extension.isEmpty()) {
                listOfFiles.append(file);
                listOfFiles.append("\n");
            } else if (file.endsWith(extension)) {
                listOfFiles.append(file);
                listOfFiles.append("\n");
            }
        }
        return listOfFiles.toString();
    }

    public static String resolveAcceptHeader(String acceptHeader) {

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
                    break;
            }
        }

        return extension;

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
        String forbiddenResponse = "\r\nHTTP/1.1 403 Forbidden";
        out.write(forbiddenResponse.getBytes());
    }

    private static void sendNotFoundResponse(OutputStream out) throws IOException {
        String notFoundResponse = "\r\nHTTP/1.1 404 Not Found";
        out.write(notFoundResponse.getBytes());
    }

    private static void sendCreatedResponse(OutputStream out) throws IOException {
        String createdResponse = "\r\nHTTP/1.1 201 Created";
        out.write(createdResponse.getBytes());
    }
}
