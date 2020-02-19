package cn.sola97.vrchat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {
    private static String proxyPatternString = "(http|socks)[5]?://(.+):(\\d+)";
    private static Pattern proxyPattern = Pattern.compile(proxyPatternString,Pattern.CASE_INSENSITIVE);
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static String[] parseProxy(String proxyString){
        Matcher m = proxyPattern.matcher(proxyString);
        if(m.find())
            return new String[] {m.group(1),m.group(2),m.group(3)};
        return new String[0];
    }
    public static Proxy getProxy(String proxy) {
        if(proxy==null || proxy.isEmpty())return null;
        String[] args = parseProxy(proxy);
        if(args.length==0)return null;
        switch (args[0].toUpperCase()){
            case "HTTP":
                return new Proxy(Proxy.Type.HTTP,new InetSocketAddress(args[1],Integer.parseInt(args[2])));
            case "SOCKS":
                return new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(args[1],Integer.parseInt(args[2])));
            default:
                logger.warn("unsupport proxy type,ignored.");
                return null;
        }
    }

    public static String[] getHostAndPort(String proxy) {
        String[] args = parseProxy(proxy);
        if (args.length == 0) return null;
        String[] ret = new String[2];
        ret[0] = args[1];
        ret[1] = args[2];
        return ret;
    }
}
