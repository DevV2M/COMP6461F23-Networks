package echo;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cURLClient {


    // Help  usage
    /** httpc help **/
    /** httpc help get **/
    /** httpc help put **/
    /** httpc help help **/


    // Get usage
    /** httpc get [-v] [-h key:value] URL **/

    //Get with query parameters
    /** httpc get 'http://httpbin.org/get?course=networking&assignment=1' **/
    // Get with verbose option
    /** httpc get -v 'http://httpbin.org/get?course=networking&assignment=1' **/


    // Post usage
    /** httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL **/

    // Post with inline data
    /** httpc post -h Content-Type:application/json --d '{"Assignment": 1}' http://httpbin.org/post **/


    /** httpc post -h Content-Type:application/json --d '{"Assignment": 1},{"Box": 1}' **/
    /** httpc post -h Content-Type:application/json --d '{"Assignment": 1},{"Box": 1}' **/

    public static void main(String[] args) {
//        curlCommands {
//                "httpc help",
//                "httpc help get",
//                "httpc help put",
//                "httpc help help",
//                "httpc get 'http://httpbin.org/get?course=networking&assignment=1'",
//                "httpc get -v 'http://httpbin.org/get?course=networking&assignment=1'",
//                "httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL",
//                "httpc post -h Content-Type:application/json --d '{"Assignment": 1}' http://httpbin.org/post",
//                "httpc post -h Content-Type:application/json --d '{"Assignment": 1},{"Box": 1}'",
//                "httpc post -h Content-Type:application/json --d '{"Assignment": 1},{"Box": 1}'"
//        }
//        String curlCommand = "httpc post -h Content-Type:application/json --d '{\"Assignment\": 1}' http://httpbin.org/post";
        String curlCommand = "httpc get 'http://httpbin.org/get?course=networking&assignment=1'";
//       String curlCommand = "httpc post -h Content-Type:application/json -h Content-Length:1123 --d '{\"Assignment\": 1}' 'http://httpbin.org/post'";
//       String curlCommand = "httpc post -h Content-Type:application/json -h Content-Length:1123 -f text.txt 'http://httpbin.org/post'";
        parseCurlCommand(curlCommand);

    }

    public static void parseCurlCommand(String curlCommand) {
        // Regular expressions for different types of commands

        // Pattern Group:Value 1:command
        String helpPattern = "httpc\\s+help(?:\\s+([a-zA-Z]+))?";

        // Pattern Group:Value 1:-v, 2:headers, 3:URL
        String getPattern = "httpc\\s+get\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+'?(http://\\S+[^'])";

        // Pattern Group:Value 1:-v, 2:headers, 3:in-line data, 4:file, 5:URL
        String postPattern = "httpc\\s+post\\s+(-v\\s)?+((?:-h\\s+[\\S]+\\s)+)?+(?:--d\\s+'(.*?)'\\s)?+(?:-f\\s+(.*?)\\s)?+'?(http://\\S+[^'])";

        // Compile regular expressions
        Pattern helpRegex = Pattern.compile(helpPattern);
        Pattern getRegex = Pattern.compile(getPattern);
        Pattern postRegex = Pattern.compile(postPattern);


        // Match the command against each pattern
        Matcher helpMatcher = helpRegex.matcher(curlCommand);
        Matcher getMatcher = getRegex.matcher(curlCommand);
        Matcher postMatcher = postRegex.matcher(curlCommand);


        System.out.println(getMatcher.find());
        System.out.println(getMatcher.group(3));
//
//        System.out.println(postMatcher.find());
//        System.out.println(postMatcher.group(4));
//        // Check which pattern matches and extract relevant information
//        if (helpMatcher.matches()) {
//            System.out.println("Command: Help");
//            if (helpMatcher.group(1) != null) {
//                System.out.println("Topic: " + helpMatcher.group(1));
//            }
//        } else if (getMatcher.matches()) {
//            System.out.println("Command: Get");
//            String verboseFlag = getMatcher.group(1);
//            String headers = getMatcher.group(2);
//            String url = getMatcher.group(3);
//            System.out.println("Verbose: " + (verboseFlag != null));
//            System.out.println("Headers: " + (headers != null ? headers.trim() : ""));
//            System.out.println("URL: " + url);
//        } else if (postMatcher.matches()) {
//            System.out.println("Command: Post");
//            String verboseFlag = postMatcher.group(1);
//            String headers = postMatcher.group(2);
//            String inlineData = postMatcher.group(3);
//            String fileFlag = postMatcher.group(4);
//            String url = postMatcher.group(5);
//            System.out.println("Verbose: " + (verboseFlag != null));
//            System.out.println("Headers: " + (headers != null ? headers.trim() : ""));
//            System.out.println("Inline Data: " + (inlineData != null ? inlineData.trim() : ""));
//            System.out.println("File: " + (fileFlag != null ? fileFlag.trim() : ""));
//            System.out.println("URL: " + url);
//        } else {
//            System.out.println("Invalid command: " + curlCommand);
//        }
//        System.out.println("-------------------------------");
    }
}
