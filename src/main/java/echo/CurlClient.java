package echo;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class CurlClient {

    public static void main(String[] args) throws IOException {
        CurlClient curlClient = new CurlClient();
    }

    public CurlClient() {
        Scanner sc = new Scanner(System.in);
        String[] inputArray = sc.nextLine().trim().split(" ");

        // Create an OptionParser
        OptionParser parser = new OptionParser();

        // Define options and arguments
        parser.accepts("httpc");
        parser.accepts("help");
        parser.accepts("get");
        parser.accepts("post");
        parser.accepts("v");
        parser.accepts("h").withRequiredArg();
        parser.accepts("url").withRequiredArg();
        parser.accepts("data").withRequiredArg();

        // Parse the input
        OptionSet opts = parser.parse(inputArray);
        System.out.println(Arrays.toString(opts.asMap().entrySet().toArray()));

        System.out.println("opts.has(\"httpc\") = " + opts.has("httpc"));

        if (!opts.has("httpc")) {
            System.out.println("Invalid command (httpc)");
        } else {
            if (opts.has("help")) {
                // httpc help
                if (opts.nonOptionArguments().isEmpty()) {
                    printGenericHelp();
                } else {
                    String helpType = (String) opts.nonOptionArguments().get(0);
                    switch (helpType) {
                        case "get":
                            printGetHelp();
                            break;
                        case "post":
                            printPostHelp();
                            break;
                        default:
                            System.out.println("Invalid command (httpc help [get/post])");
                    }
                }
            } else if (opts.has("get")) {
                // http get [url] [-v]
                boolean isVerbose = opts.has("v");
                String url = (String) opts.valueOf("url");
                if (isVerbose) {
                    httpUtility.getVerbose(url);
                } else {
                    httpUtility.get(url);
                }
            } else if (opts.has("post")) {
                // http post [url] [-v] [-d data] [-h headers]
                boolean isVerbose = opts.has("v");
                String url = (String) opts.valueOf("url");
                String data = (String) opts.valueOf("data");
                String headers = (String) opts.valueOf("h");
                if (isVerbose) {
                    httpUtility.postVerbose(url, data, headers);
                } else {
                    httpUtility.post(url, data, headers);
                }
            } else {
                System.out.println("MASTER Invalid command");
            }
        }
    }

    public static void printGenericHelp() {
        String helpMessage = """
                                
                httpc is a curl-like application but supports HTTP protocol only.
                Usage:
                    httpc command [arguments]
                The commands are:
                    get     executes a HTTP GET request and prints the response.
                    post    executes a HTTP POST request and prints the response.
                    help    prints this screen.
                Use "httpc help [command]" for more information about a command.
                """;

        System.out.println(helpMessage);
    }

    public static void printGetHelp() {
        String getHelpMessage = """
                                
                usage: httpc get [-v] [-h key:value] URL
                                
                Get executes a HTTP GET request for a given URL.
                                
                -v Prints the detail of the response such as protocol, status, and headers.
                -h key:value Associates headers to HTTP Request with the format 'key:value'.
                """;

        System.out.println(getHelpMessage);
    }

    public static void printPostHelp() {
        String getPostMessage = """
                                
                usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL
                                
                Post executes a HTTP POST request for a given URL with inline data or from file.
                                
                    -v Prints the detail of the response such as protocol, status, and headers.
                    -h key:value Associates headers to HTTP Request with the format 'key:value'.
                    -d string Associates an inline data to the body HTTP POST request.
                    -f file Associates the content of a file to the body HTTP POST request.
                                
                Either [-d] or [-f] can be used but not both.
                """;

        System.out.println(getPostMessage);
    }
}
