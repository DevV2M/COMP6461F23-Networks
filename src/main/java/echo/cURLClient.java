package echo;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static echo.httpLibrary.postFile;

public class cURLClient {

    // cURL Commands

    // Help
    // httpc help
    // httpc help get
    // httpc help post

    // Get
    // httpc get -v -h Content-Type:application/json 'http://httpbin.org/get?course=networking&assignment=1'
    // httpc get -v http://httpbin.org/status/418
    // httpc get http://httpbin.org/status/418
    // httpc get -v http://httpbin.org/status/418 -o ./teapot.txt

    // Redirect sample: httpc get -v http://httpbin.org/status/301
    // httpc get -v http://httpbin.org/status/304
    // Redirect post: httpc post -v --d '{:}' http://httpbin.org/status/301

    // Post in-line data:
    // httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": 1}' http://httpbin.org/post
    // httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": "Student"}' http://httpbin.org/post
    // httpc post -v -h Content-Type:application/json --d 'hello' http://httpbin.org/post

    // Post in-line data A2
    // httpc post -v -h overwrite:true --d 'hello' /teapot.txt

    // Post file
    // httpc post -v -f ./text.txt 'http://httpbin.org/post' -o postFileTest.txt

    // Post file A2
    // httpc post -v -f ./teapot.txt /teapot.txt -o postFileTest.txt
    // Regular expressions for different types of commands

    private static String helpPattern = "httpc\\s+help(?:\\s+([a-zA-Z]+))?";

    // Pattern Group:Value 1:-v, 2:headers, 3:URL
//    private static String getPattern = "httpc\\s+get\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+'?(https?://\\S+[^'])+\\s*(?:-o\\s(\\S+\\.txt))?";
    private static String getPattern = "httpc\\s+get\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+(?:'?(https?://\\S+[^']))?(?:(/\\S*))?+\\s*(?:-o\\s(\\S+\\.txt))?";
    // Pattern Group:Value 1:-v, 2:headers, 3:in-line data, 4:file, 5:URL
    private static String postPattern = "httpc\\s+post\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+(?:--d\\s+'(.*?)'\\s)?+(?:-f\\s+(.*?)\\s)?+(?:'?(https?://\\S+[^']))?(?:(/\\S*))?+\\s*(?:-o\\s(\\S+\\.txt))?";

    // Compile regular expressions
    private static Pattern helpRegex = Pattern.compile(helpPattern);
    private static Pattern getRegex = Pattern.compile(getPattern);
    private static Pattern postRegex = Pattern.compile(postPattern);

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        String curlCommand = sc.nextLine().trim();
        runCommand(curlCommand);

    }

    public static void runCommand(String curlCommand) throws IOException {

        // Match the command against each pattern
        Matcher helpMatcher = helpRegex.matcher(curlCommand);
        Matcher getMatcher = getRegex.matcher(curlCommand);
        Matcher postMatcher = postRegex.matcher(curlCommand);


        // Check which pattern matches and extract relevant information
        if (helpMatcher.find()) {
            if (helpMatcher.group(1) != null) {
                printHelp(helpMatcher.group(1));
            } else printHelp("");
        } else if (postMatcher.find()) {
            String verboseFlag = postMatcher.group(1);
            String headerData = postMatcher.group(2);
            String inlineData = postMatcher.group(3);
            String fileFlag = postMatcher.group(4);
            String url;
            Socket clientSocket;
            if(postMatcher.group(5) != null) {
                url = postMatcher.group(5).trim();
                clientSocket = httpLibrary.getSocket(url);
            }
            else {
                url = postMatcher.group(6).trim();
                clientSocket  = new Socket("localhost", 8080);
            }
            String outputFile = postMatcher.group(7);
            if (inlineData != null) {
                System.out.println("Here in line");
                httpLibrary.post(inlineData.trim(), httpLibrary.getPathToResource(url), clientSocket, getHeaders(headerData), (verboseFlag != null), outputFile);
            } else if (fileFlag != null){
                System.out.println("here file post");
                httpLibrary.postFile(fileFlag.trim(), clientSocket, getHeaders(headerData), (verboseFlag != null), outputFile);
            }
        } else if (getMatcher.find()) {
            String verboseFlag = getMatcher.group(1);
            String headerData = getMatcher.group(2);
            String url;
            Socket clientSocket = null;
            if(getMatcher.group(3) != null) {
                url = getMatcher.group(3).trim();
                clientSocket = httpLibrary.getSocket(url);
            }
            else {
                url = getMatcher.group(4).trim();
                clientSocket  = new Socket("localhost", 8080);
            }
            String outputFile = getMatcher.group(5);
            httpLibrary.get(httpLibrary.getPathToResource(url), clientSocket, getHeaders(headerData), (verboseFlag != null), outputFile);
        }  else {
            System.out.println("Invalid command: " + curlCommand);
        }
    }

    public static void printHelp(String type) {

        if (type.compareTo("") == 0) {
            System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n" +
                    "Usage:\n" +
                    "    httpc command [arguments]\n" +
                    "The commands are:\n" +
                    "    get     executes a HTTP GET request and prints the response.\n" +
                    "    post    executes a HTTP POST request and prints the response.\n" +
                    "    help    prints this screen.\n" +
                    "Use \"httpc help [command]\" for more information about a command.");
        } else if (type.compareTo("get") == 0) {
            System.out.println("usage: httpc get [-v] [-h key:value] URL [-o filename]\n" +
                    "Get executes a HTTP GET request for a given URL.\n" +
                    "   -v Prints the detail of the response such as protocol, status, and headers.\n" +
                    "   -h key:value Associates headers to HTTP Request with the format 'key:value'.\n" +
                    "   -o write the body of the HTTP response to the specified file instead of console.\n");
        } else if (type.compareTo("post") == 0) {
            System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL [-o filename]\n" +
                    "Post executes a HTTP POST request for a given URL with inline data or from file.\n" +
                    "   -v Prints the detail of the response such as protocol, status, and headers.\n" +
                    "   -h key:value Associates headers to HTTP Request with the format 'key:value'.\n" +
                    "   -d string Associates an inline data to the body HTTP POST request.\n" +
                    "   -f file Associates the content of a file to the body HTTP POST request.\n" +
                    "   -o write the body of the HTTP response to the specified file instead of console.\n" +
                    "Either [-d] or [-f] can be used but not both.");
        }
    }

    public static Map<String, String> getHeaders(String cURLHeaderString) {

        Map<String, String> headers = new HashMap<>();

        if (cURLHeaderString != null) {
            String[] cURLHeaderData = cURLHeaderString.split("\\s+");

            for (String header : cURLHeaderData) {
                if (header.compareTo("-h") == 0) continue;
                String[] keyValue = header.split(":");
                headers.put(keyValue[0], keyValue[1]);
            }
        }
        return headers;
    }
}
