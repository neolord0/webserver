package kr.dogfoot.webserver.util;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.context.connection.ajp.AjpProxyConnection;
import kr.dogfoot.webserver.context.connection.http.client.HttpClientConnection;
import kr.dogfoot.webserver.context.connection.http.proxy.HttpProxyConnection;

import java.util.Date;

public class Message {
    private static boolean enableDebug = true;

    public static void enableDebug(boolean enableDebug) {
        Message.enableDebug = enableDebug;
    }

    public static void debug(String msg) {
        if (enableDebug == false) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Date now = new Date();
        sb.append("Debug : Common - ")
                .append(now.getHours()).append(':').append(now.getMinutes()).append(':').append(now.getSeconds())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void debug(Context context, String msg) {
        if (enableDebug == false) {
            return;
        }
        debug(context.clientConnection(), msg);
    }

    public static void debugWithInterval(Context context, String msg) {
        if (enableDebug == false) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Debug : Http(")
                .append(context.clientConnection().id())
                .append(") : ")
                .append(context.debugInfo().getInterval())
                .append(" - ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void debug(HttpClientConnection client, String msg) {
        if (enableDebug == false) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Date now = new Date();
        sb.append("Debug : Client(")
                .append(client.id())
                .append(") - ")
                .append(now.getHours()).append(':').append(now.getMinutes()).append(':').append(now.getSeconds())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void debug(AjpProxyConnection ajpProxy, String msg) {
        if (enableDebug == false) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Date now = new Date();
        sb.append("Debug : ");
        if (ajpProxy.context() != null && ajpProxy.context().clientConnection() != null) {
            sb.append("Client(").append(ajpProxy.context().clientConnection().id()).append(") ");
        }
        sb.append("AjpProxy(")
                .append(ajpProxy.id())
                .append(") - ")
                .append(now.getHours()).append(':').append(now.getMinutes()).append(':').append(now.getSeconds())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void debug(HttpProxyConnection httpProxy, String msg) {
        if (enableDebug == false) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Date now = new Date();
        sb.append("Debug : ");
        if (httpProxy.context() != null && httpProxy.context().clientConnection() != null) {
            sb.append("Client(").append(httpProxy.context().clientConnection().id()).append(") ");
        }
        sb.append("HttpProxy(")
                .append(httpProxy.id())
                .append(") - ")
                .append(now.getHours()).append(':').append(now.getMinutes()).append(':').append(now.getSeconds())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }


    public static void log(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("log : Common - ")
                .append(new Date())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void warn(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("warn : Common - ")
                .append(new Date())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

    public static void error(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("Error : ")
                .append(new Date())
                .append(" : ")
                .append(msg);
        System.out.println(sb.toString());
    }

}
