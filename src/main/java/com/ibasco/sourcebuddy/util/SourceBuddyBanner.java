package com.ibasco.sourcebuddy.util;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class SourceBuddyBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        String text = "   _____                            ____            _     _       \n" +
                "  / ____|                          |  _ \\          | |   | |      \n" +
                " | (___   ___  _   _ _ __ ___ ___  | |_) |_   _  __| | __| |_   _ \n" +
                "  \\___ \\ / _ \\| | | | '__/ __/ _ \\ |  _ <| | | |/ _` |/ _` | | | |\n" +
                "  ____) | (_) | |_| | | | (_|  __/ | |_) | |_| | (_| | (_| | |_| |\n" +
                " |_____/ \\___/ \\__,_|_|  \\___\\___| |____/ \\__,_|\\__,_|\\__,_|\\__, |\n" +
                "                                                             __/ |\n" +
                "                                                            |___/ \n" +
                "\n";
        out.print(text);
        out.println("Powered by Spring Framework\n");
    }
}
