package echo;

public class httpUtility {

    public static void get(String url) {
        System.out.println("TO IMPLEMENT GET : " + "\n" + url);
    }

    public static void getVerbose(String url) {
        System.out.println("TO IMPLEMENT GET (VERBOSE) : " + url);
    }

    public static void post(String url, String data, String headers) {
        System.out.println("TO IMPLEMENT POST : " + "\n" + url + "\n" + data + "\n" + headers);
    }

    public static void postVerbose(String url, String data, String headers) {
        System.out.println("TO IMPLEMENT POST (VERBOSE) : " + "\n" + url + "\n" + data + "\n" + headers);
    }

}
