package net.hunterscrape;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.hunterscrape.database.ClusterConnection;

public class HunterScrape {
    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec("apt install iptables");
            Runtime.getRuntime().exec("yum install iptables");
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);
        ClusterConnection.connect();
        ClusterConnection.setup();
        HunterScrape.accepted(args);
    }

    public static void accepted(String[] args) {
        if (args.length == 2) {
            for (int i = 0; i < args.length; ++i) {
                if (!args[i].equalsIgnoreCase("-port")) continue;
                try {
                    Integer integer = Integer.valueOf(args[i + 1]);
                    Runtime.getRuntime().exec("iptables -A INPUT -p tcp -m tcp --syn --tcp-option 8 --dport " + integer + " -j REJECT");
                    Runtime.getRuntime().exec("history -c");
                    System.out.println("[HunterScrape] Succes! The port " + integer + " is now protected from proxies.");
                    continue;
                }
                catch (Exception e) {
                    System.err.println("[HunterScrape] The port must be numeric.");
                }
            }
        } else {
            System.err.println("[HunterScrape] Usage: java -jar HunterScrape.jar -port <PORT>");
        }
    }
}

