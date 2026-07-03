//package com.projeto;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class BrowserConfig {
//
//    @Bean
//    public CommandLineRunner openBrowser() {
//        return args -> {
//            String url = "http://localhost:8080";
//            try {
//                String os = System.getProperty("os.name").toLowerCase();
//                if (os.contains("win")) {
//                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
//                } else if (os.contains("mac")) {
//                    Runtime.getRuntime().exec("open " + url);
//                } else if (os.contains("nix") || os.contains("nux")) {
//                    Runtime.getRuntime().exec("xdg-open " + url);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }
//}