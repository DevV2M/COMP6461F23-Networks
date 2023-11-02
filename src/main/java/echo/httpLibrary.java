/**
 * COMP 6461 - Computer Networks and Protocols
 * Lab Assignment #1
 * Group Members:
 * Vithu Maheswaran - 27052715
 * Shafiq Imtiaz - 40159305
 */

package echo;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class httpLibrary {

    public static void get(String path, Socket socket, Map<String, String> headers, boolean verbose, String outputFilePath) {

        try {
            StringBuilder request = new StringBuilder()
                    .append(String.format("GET %s HTTP/1.0\r\n", path))
                    .append(String.format("Host: %s\r\n", socket.getInetAddress().getHostName()));

            // Add custom headers to the request
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            request.append("\r\n");

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Request: \n" + request);
            writeToSocket(wr, request.toString());
            String[] response = readFromSocket(rd);

            if (verbose) {
                for (String str : response) {
                    System.out.println(str);
                }
            } else {
                System.out.println(response[1]);
            }

            if (extractStatusCode(response[0]).compareTo("301") == 0 || extractStatusCode(response[0]).compareTo("302") == 0) {
                System.out.println("--------------- Redirecting -----------------");
                String url = extractLocation(response[0]);

                System.out.println("Break Point 2");

                String newURL = "http://" + socket.getInetAddress().getHostName() + url;
                get(url, getSocket(newURL), headers, verbose, outputFilePath);

            }

            // Option Task 2: Write response body to file
            if (outputFilePath != null) {
                writeResponseBodyToFile(response[1], outputFilePath);
            }

            wr.close();
            rd.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void post(String data, String path, Socket socket, Map<String, String> headers, boolean verbose, String outputFilePath) throws IOException {

        System.out.println("URL: " + path);
        StringBuilder request = new StringBuilder()
                .append(String.format("POST %s HTTP/1.0\r\n", path))
                .append(String.format("Content-Length: %s\r\n", data.length()));

        System.out.println();
        // Add custom headers to the request
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        request.append("\r\n");
        request.append(data);

        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Request: \n" + request.toString());
        System.out.println("End Request");
        writeToSocket(wr, request.toString());
        String[] response = readFromSocket(rd);

        if (verbose) {
            for (String str : response) {
                System.out.println(str);
            }
        } else {
            System.out.println(response[1]);
        }

        if (extractStatusCode(response[0]).compareTo("301") == 0 || extractStatusCode(response[0]).compareTo("302") == 0) {
            System.out.println("--------------- Redirecting -----------------");
            String url = extractLocation(response[0]);
            String newURL = "http://" + socket.getInetAddress().getHostName() + url;
            post(data, url, getSocket(newURL), headers, verbose, outputFilePath);

        }

        // Option Task 2: Write response body to file
        if (outputFilePath != null) {
            writeResponseBodyToFile(response[1], outputFilePath);
        }

        wr.close();
        rd.close();
    }

    public static void postFile(String url, String filePath, Socket socket, Map<String, String> headers, Boolean verbose, String outputFilePath) {

        try (OutputStream os = socket.getOutputStream()) {
            String boundary = "----WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
            String requestBody = getRequestBodyForPostFile(filePath, boundary);
            String requestHeader = getRequestHeaderForPostFile(url, socket, requestBody.getBytes().length, headers, boundary);
            System.out.println("Request Header: \n" + requestHeader);
            System.out.println("Request Body: \n" + requestBody);

            // Post the request with file content
            String combinedRequest = requestHeader + requestBody;
//            os.write(requestHeader.getBytes());
//            os.write(requestBody.getBytes());
            os.write(combinedRequest.getBytes());
            os.flush();

            // Read and print the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] response = readFromSocket(rd);

            if (verbose) {
                for (String str : response) {
                    System.out.println(str);
                }
            } else {
                System.out.println(response[1]);
            }

            // Option Task 2: Write response body to file
            if (outputFilePath != null) {
                writeResponseBodyToFile(response[1], outputFilePath);
            }

            os.close();
            rd.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRequestBodyForPostFile(String filePath, String boundary) throws IOException {

        FileInputStream fis = new FileInputStream(filePath);
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
        requestBodyBuilder.append(String.format("Content-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n", getFileNameFromPath(filePath)));
        requestBodyBuilder.append("Content-Type: application/octet-stream\r\n");
        requestBodyBuilder.append("\r\n");
        requestBodyBuilder.append(allData);
        requestBodyBuilder.append("\r\n").append("--").append(boundary).append("--\r\n");
        return requestBodyBuilder.toString();
    }

    private static String getRequestHeaderForPostFile(String path, Socket socket, int size, Map<String, String> headers, String boundary) {

        StringBuilder requestHeader = new StringBuilder();
        requestHeader.append("POST " + path + " HTTP/1.0\r\n")
                .append(String.format("Host: %s\r\n", socket.getInetAddress().getHostName()));

        // Add custom headers to the request
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestHeader.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        requestHeader.append(String.format("Content-Length: %d\r\n", size))
                .append(String.format("Content-Type: multipart/form-data; boundary=%s\r\n\r\n", boundary));

        return requestHeader.toString();
    }

    private static void writeToSocket(BufferedWriter wr, String request) throws IOException {
        wr.write(request.toString());
        wr.flush();
    }

    private static String[] readFromSocket(BufferedReader rd) throws IOException {
        String line;
        StringBuilder response = new StringBuilder();
        String[] responseHeaderAndBody = new String[2];
        boolean firstEmptyLine = false;
        while ((line = rd.readLine()) != null) {
            if (line.trim().isEmpty() && firstEmptyLine == false) {
                firstEmptyLine = true;
                responseHeaderAndBody[0] = response.toString();
                response = new StringBuilder();
                continue;
            }
            response.append(line).append("\r\n");
        }
        responseHeaderAndBody[1] = response.toString();
        return responseHeaderAndBody;
    }

    public static String getPathToResource(String urlStr) {
        URI uri = null;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
    }

    public static Socket getSocket(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        int port;
        if ((port = url.getPort()) == -1) port = url.getDefaultPort();

        String host = url.getHost();

        try {
            return new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractStatusCode(String header) {

        // Define a regular expression pattern to match the status code.
        Pattern pattern = Pattern.compile("HTTP/\\d+\\.\\d+\\s(\\d+)\\s.*");

        // Use a Matcher to find the pattern in the input text.
        Matcher matcher = pattern.matcher(header);

        if (matcher.find()) {
            // Extract and return the matched group (the value of the status code).
            String statusCode = matcher.group(1);
            return statusCode;
        } else {
            // Return an empty string if the status code is not found.
            return "";
        }
    }

    private static String extractLocation(String header) {

        // Define a regular expression pattern to match the status code.
        Pattern pattern = Pattern.compile("[lL]ocation:\\s(\\S+)");

        // Use a Matcher to find the pattern in the response header.
        Matcher matcher = pattern.matcher(header);

        if (matcher.find()) {
            // Extract and return the matched group (the value of the Location).
            String location = matcher.group(1);
            return location;
        } else {
            // Return an empty string if the status code is not found.
            return "";
        }
    }

    private static String getFileNameFromPath(String path) {

        Pattern fileNameRegex = Pattern.compile("([^/\\\\]+)$");

        Matcher fileNameMatcher = fileNameRegex.matcher(path);
        fileNameMatcher.find();
        String fileName = fileNameMatcher.group(1);
        return fileName != null ? fileName : "";
    }

    private static void writeResponseBodyToFile(String responseBody, String filePath) {

        try {
            // Create a FileWriter with the specified file path.
            FileWriter fileWriter = new FileWriter(filePath);

            // Create a BufferedWriter for efficient writing.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write the string data to the file.
            bufferedWriter.write(responseBody);

            // Close the BufferedWriter to flush and close the file.
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}