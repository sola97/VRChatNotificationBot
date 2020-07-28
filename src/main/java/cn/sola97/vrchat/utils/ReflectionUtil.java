package cn.sola97.vrchat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;


public class ReflectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

    public static HttpURLConnection getHttpURLConnection(ClientHttpResponse clientHttpResponse) {
        if (clientHttpResponse.getClass().getSimpleName().equals("SimpleClientHttpResponse")) {
            try {
                Field field = clientHttpResponse.getClass().getDeclaredField("connection");
                field.setAccessible(true);
                HttpURLConnection connection = (HttpURLConnection) field.get(clientHttpResponse);
                return connection;
            } catch (NoSuchFieldException e) {
                logger.error("获取HttpURLConnection失败", e);
            } catch (IllegalAccessException e) {
                logger.error("Access HttpURLConnection失败", e);
            }
        }
        return null;
    }

    public static DelegateHttpsURLConnection getDelegateHttpsURLConnection(HttpURLConnection httpURLConnection) {
        if (httpURLConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            try {
                Field field = httpsURLConnection.getClass().getDeclaredField("delegate");
                field.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) field.get(httpsURLConnection);
                return delegateHttpsURLConnection;
            } catch (NoSuchFieldException e) {
                logger.error("获取getDelegateHttpsURLConnection失败", e);
            } catch (IllegalAccessException e) {
                logger.error("Access getDelegateHttpsURLConnection失败", e);
            } catch (Exception e) {
                logger.error("error", e);
            }
            return null;
        }
        return null;
    }


    public static MessageHeader getMessageHeader(DelegateHttpsURLConnection delegateHttpsURLConnection) {
        try {
            sun.net.www.protocol.http.HttpURLConnection connection = delegateHttpsURLConnection;
            Class<?> aClass = Class.forName("sun.net.www.protocol.http.HttpURLConnection");
            Field field = aClass.getDeclaredField("requests");
            field.setAccessible(true);
            MessageHeader messageHeader = (MessageHeader) field.get(connection);
            return messageHeader;
        } catch (NoSuchFieldException e) {
            logger.error("获取MessageHeader失败", e);
        } catch (IllegalAccessException e) {
            logger.error("Access MessageHeader失败", e);
        } catch (ClassNotFoundException e) {
            logger.error("getMessageHeader", e);
        }
        return null;
    }
}
