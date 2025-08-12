package big.engine.math.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PercentEncoder {
    public static String encodeChinese(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8)
                                   .replace("+", "%20");
    }
    public static String decodeChinese(String encodedInput) {
        return URLDecoder.decode(encodedInput, StandardCharsets.UTF_8);
    }
    public static void main(String[] args) {

    }
}