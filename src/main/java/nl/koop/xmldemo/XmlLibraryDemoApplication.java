package nl.koop.xmldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XmlLibraryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlLibraryDemoApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("✅ XML Library Demo gestart!");
        System.out.println("🌐 Open: http://localhost:8080");
        System.out.println("==============================================\n");
    }
}