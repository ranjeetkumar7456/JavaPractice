package utils;


import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {

    public static void log(String message) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + time + "] [INFO] " + message);
    }

    public static void error(String message, Exception e) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.err.println("[" + time + "] [ERROR] " + message);
        e.printStackTrace();
    }
}

