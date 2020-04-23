package com.hsn;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) throws InterruptedException {
        String s = Start.class.getResource("chromedriver.exe").toString();
        System.getProperties().setProperty("webdriver.chrome.driver","F:\\workspace\\idea\\selenuim\\src\\main\\resources\\chromedriver.exe");
        Work work = new Work();
        Scanner scanner = new Scanner(System.in);
        while (true){
            String next = scanner.next();
            if ("q".equals(next)){
                work.quit();
                break;
            }else if ("s".equals(next)){
                work.start();
            }
        }
    }
}
