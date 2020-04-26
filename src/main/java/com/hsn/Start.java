package com.hsn;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URL;
import java.util.Scanner;

public class Start {
    public static void main(String[] args){
        //打包时用此条
//        System.getProperties().setProperty("webdriver.chrome.driver", "chromedriver.exe");

        System.getProperties().setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        Work work = new Work();
        Scanner scanner = new Scanner(System.in);
        System.out.println("进入网页后，输入's'开始：");
        while (true) {
            String next = scanner.next();
            if ("q".equals(next)) {
                work.quit();
                break;
            } else if ("s".equals(next)) {
                work.start();
            }
        }

    }
}
