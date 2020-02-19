package cn.sola97.vrchat.utils;

import org.springframework.http.HttpHeaders;

import java.net.HttpCookie;
import java.util.*;

public class CookieUtil {
    public static String processHeaders(HttpHeaders headers) {
        List<HttpCookie> cookies = new ArrayList<>();
        final List<String> cooks = headers.get("Set-Cookie");
        if (cooks != null && !cooks.isEmpty()) {
            cooks.stream().map((c) -> HttpCookie.parse(c)).forEachOrdered((cook) -> {
                cook.forEach((a) -> {
                    HttpCookie cookieExists = cookies.stream().filter(x -> a.getName().equals(x.getName())).findAny().orElse(null);
                    if (cookieExists != null) {
                        cookies.remove(cookieExists);
                    }
                    cookies.add(a);
                });
            });
        }
        StringBuffer sb = new StringBuffer();
        for (HttpCookie cookie : cookies) {
            sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }
        return sb.toString();
    }
    public static Map<String,String> convertStringToMap(String cookie){
        HashMap<String, String> map = new HashMap<>();
        String[] cooks = cookie.split(";");
        for (String cook : cooks) {
            String[] split = cook.split("=");
            map.put(split[0],split[1]);
        }
        return map;
    }
}
