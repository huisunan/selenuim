package com.hsn;

import com.google.common.io.Files;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Work {
    private WebDriver webDriver;
    private static final String url = "http://i.mooc.chaoxing.com/";
    private String title;
    private String value;
    private Set<String> haveStudy = new HashSet<>();
    private JTextPane textPane;
    public Work() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=C:\\User Data");
        this.webDriver = new ChromeDriver(chromeOptions);

        this.webDriver.get(url);
    }
    public Work(JTextPane textPane){
        this();
        this.textPane = textPane;
    }
    public void quit() {
        this.webDriver.quit();
    }

    private void moveAndClick(WebElement element) {
        Dimension size = element.getSize();
        int xOffset = (int) (size.getWidth() * (1 - Math.random() * 2) / 2);
        int yOffset = (int) (size.getHeight() * (1 - Math.random() * 2) / 2);
        new Actions(webDriver).moveToElement(element, xOffset, yOffset).click().perform();
    }

    private void moveAndClickVideo(WebElement video) {
        Dimension size = video.getSize();
        int xOffset = (int) (size.getWidth() * (1 - Math.random() * 2) / 2);
        int yOffset = (int) ((size.getHeight() - 100) * (1 - Math.random() * 2) / 2);
        new Actions(webDriver).moveToElement(video, xOffset, yOffset).click().perform();
    }

    /**
     * 切换到视频tab页
     */
    private void clickTab() throws InterruptedException {
        //获取tab页
        WebElement tab = webDriver.findElement(By.cssSelector(".tabtags"));
        List<WebElement> spans = tab.findElements(By.cssSelector("span"));
        for (WebElement span : spans) {
            String title = span.getAttribute("title");
            if ("视频".equals(title)) {
                //点击tab页
                moveAndClick(span);
                Thread.sleep(1000);
                break;
            }
        }
    }

    /**
     * 返回true表示看过了
     */
    private boolean playVideo() throws InterruptedException {
        /**
         * 滚动进度条
         */
        ((JavascriptExecutor) webDriver).executeScript("window.scrollTo(0,250);");
        Thread.sleep(300);
        //进入 iframe
        WebElement iframe = webDriver.findElement(By.tagName("iframe"));
        webDriver.switchTo().frame(iframe);
        try{
            //找到了就是学过了
            webDriver.findElement(By.cssSelector(".ans-job-finished"));
            haveStudy.add(title);
            return true;
        }catch (NoSuchElementException ignored){
            //没有找到就是没学过

        }
        Thread.sleep(500);
        WebElement iframe2 = webDriver.findElement(By.tagName("iframe"));
        webDriver.switchTo().frame(iframe2);
        Thread.sleep(500);
        //点击播放按钮
        WebElement videoButton = webDriver.findElement(By.cssSelector(".vjs-big-play-button"));
        moveAndClickVideo(videoButton);
        Thread.sleep(500);

        return false;
    }

    /**
     * 自动答题
     */
    private boolean fillAnswer() {
        boolean result = false;
        try {

            WebElement opts = webDriver.findElement(By.cssSelector(".ans-videoquiz-opts"));
            result = true;
            List<WebElement> labels = opts.findElements(By.tagName("label"));
            for (WebElement label : labels) {
                WebElement input = label.findElement(By.tagName("input"));
                if (input.getAttribute("value").equals("true")) {
                    moveAndClick(input);
                    Thread.sleep(1000);
                    WebElement submit = webDriver.findElement(By.cssSelector(".ans-videoquiz-submit"));
                    moveAndClick(submit);
                    Thread.sleep(500);
                    break;
                }
            }
        } catch (NoSuchElementException | InterruptedException ignored) {
        }
        return result;
    }

    /**
     * 视频被暂停  恢复
     */
    private void recoveryVideo(boolean haveAns) {
        if (!haveAns) {
            try {
                //视频被暂停
                WebElement video = webDriver.findElement(By.cssSelector("#video_html5_api"));

                webDriver.findElement(By.cssSelector(".vjs-paused"));
                moveAndClickVideo(video);
            } catch (NoSuchElementException ignored) {

            }
        }
    }

    /**
     * 检测视频是否结束
     */
    private void listener() throws InterruptedException {
        //获取进度条
        WebElement progress = webDriver.findElement(By.cssSelector(".vjs-progress-holder"));
        String value = progress.getAttribute("aria-valuenow");
        //获取视频播放器
        while (!value.startsWith("100")) {
            //出现答题卡
            boolean haveAns = fillAnswer();
            recoveryVideo(haveAns);
            Thread.sleep(1000);
            //更新进度条的值
            value = progress.getAttribute("aria-valuenow");
            System.out.print("\r" + title + "     " + value + "%");
            if (textPane != null){
                textPane.setText(title + "     " + value + "%");
            }
//            File screenshotAs = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
          /*  try{
                File to = new File("截图.jpg");
                Files.copy(screenshotAs,to);
            }catch (Exception e){
                System.out.println("保存图片失败!");
            }*/
        }
        Thread.sleep(3000);

    }

    private void switchWindow() throws InterruptedException {
        Set<String> handles = webDriver.getWindowHandles();
        //窗口切换
        boolean find = false;
        for (String handle : handles) {
            webDriver.switchTo().window(handle);
            if (webDriver.getCurrentUrl().contains("/mycourse/studentstudy")) {
                find = true;
                break;
            } else {
                webDriver.close();
            }
        }
        if (!find) {
            throw new RuntimeException("没有找到学习窗口");
        }
        Thread.sleep(3000);
    }

    /**
     * 开始学习模式
     */
    private void startStudy() throws InterruptedException {
        //获取未完成的列表
        while (true) {
            webDriver.switchTo().defaultContent();
            int offset = -10;
            //content 33px
            //ncells 27px
            //document.querySelector("#content1").scrollTo(0,100)
            List<WebElement> cells = webDriver.findElements(By.cssSelector(".cells"));
            WebElement curCell = null;
            boolean find = false;
            for (WebElement cell : cells) {
                if (find) {
                    break;
                }
                offset += 33;
                List<WebElement> elements = cell.findElements(By.cssSelector(".ncells"));
                for (WebElement element : elements) {
                    String attribute = element.findElement(By.tagName("a")).getAttribute("title");
                    if ("2".equals(element.findElement(By.cssSelector(".roundpointStudent")).getText()) && !haveStudy.contains(attribute)) {
                        curCell = element;
                        find = true;
                        break;
                    }
                    haveStudy.add(attribute);
                    offset += 27;
                }
            }
            if (curCell != null) {
                ((JavascriptExecutor) webDriver).executeScript("document.querySelector(\"#content1\").scrollTo(0," + offset + ")");
                Thread.sleep(800);
                WebElement href = curCell.findElement(By.tagName("a"));
                moveAndClick(href);
                String temp = title;
                title = href.getAttribute("title");
                if (temp != null && temp.equals(title)){
                    System.out.println("重复学习了!!!!");
                }
                Thread.sleep(1000);
                //切换选项卡
                this.clickTab();
                //播放视频
                if (playVideo()){
                    continue;
                }
                //开启监听
                this.listener();
            } else {
                break;
            }
        }
    }

    public void start() {
        try {
            this.switchWindow();
            this.startStudy();
        } catch (Exception e) {
            e.printStackTrace();
//            Scanner scanner = new Scanner(System.in);
//            System.out.println("系统出错是否退出?\ny:退出");
//            String next = scanner.next();
//            if (next.equals("y")) {
//                webDriver.quit();
//            }
        }
    }

}
