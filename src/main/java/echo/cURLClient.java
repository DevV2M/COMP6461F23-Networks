package echo;

import java.io.IOException;
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
    // httpc get -v 'http://httpbin.org/status/418'
    // httpc get http://httpbin.org/status/418

    // Post in-line data:
    // httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": 1}' http://httpbin.org/post
    // httpc post -v -h Content-Type:application/json --d '{"Assignment": 1,"Vithu": "Student"}' http://httpbin.org/post
    // httpc post -v -h Content-Type:application/json --d 'hello' http://httpbin.org/post

    // Post file
    // httpc post -v -f ./text.txt 'http://httpbin.org/post'

    // Regular expressions for different types of commands

    // Pattern Group:Value 1:command
    private static String helpPattern = "httpc\\s+help(?:\\s+([a-zA-Z]+))?";

    // Pattern Group:Value 1:-v, 2:headers, 3:URL
    private static String getPattern = "httpc\\s+get\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+'?(http://\\S+[^'])";

    // Pattern Group:Value 1:-v, 2:headers, 3:in-line data, 4:file, 5:URL
    private static String postPattern = "httpc\\s+post\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+(?:--d\\s+'(.*?)'\\s)?+(?:-f\\s+(.*?)\\s)?+'?(http://\\S+[^'])";

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
        } else if (getMatcher.find()) {
            String verboseFlag = getMatcher.group(1);
            String headerData = getMatcher.group(2);
            String url = getMatcher.group(3);
            httpLibrary.get(httpLibrary.getPathToResource(url), httpLibrary.getSocket(url), getHeaders(headerData), (verboseFlag != null));
        } else if (postMatcher.find()) {
            String verboseFlag = postMatcher.group(1);
            String headerData = postMatcher.group(2);
            String inlineData = postMatcher.group(3);
            String fileFlag = postMatcher.group(4);
            String url = postMatcher.group(5);

            if (inlineData != null) {
                httpLibrary.post(inlineData.trim(), httpLibrary.getPathToResource(url), httpLibrary.getSocket(url), getHeaders(headerData), (verboseFlag != null));
            } else if (fileFlag != null){

                httpLibrary.postFile(fileFlag.trim(), httpLibrary.getSocket(url), getHeaders(headerData), (verboseFlag != null));
            }
        } else {
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
            System.out.println("usage: httpc get [-v] [-h key:value] URL\n" +
                    "Get executes a HTTP GET request for a given URL.\n" +
                    "   -v Prints the detail of the response such as protocol, status, and headers.\n" +
                    "   -h key:value Associates headers to HTTP Request with the format 'key:value'.\n");
        } else if (type.compareTo("post") == 0) {
            System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n" +
                    "Post executes a HTTP POST request for a given URL with inline data or from file.\n" +
                    "   -v Prints the detail of the response such as protocol, status, and headers.\n" +
                    "   -h key:value Associates headers to HTTP Request with the format 'key:value'.\n" +
                    "   -d string Associates an inline data to the body HTTP POST request.\n" +
                    "   -f file Associates the content of a file to the body HTTP POST request.\n" +
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
