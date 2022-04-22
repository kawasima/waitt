package net.unit8.waitt.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author kawasima
 */
@SpringBootApplication
public class ExampleServletApplication extends SpringBootServletInitializer {
    public static void main(String... args)  {
        SpringApplication.run(ExampleServletApplication.class, args);
    }
}
