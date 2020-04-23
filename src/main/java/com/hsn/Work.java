package com.hsn;

import org.omg.PortableServer.THREAD_POLICY_ID;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Work {
    private WebDriver webDriver;
    private static final String url = "http://i.mooc.chaoxing.com/";

    public Work(){
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=C:\\User Data");
        this.webDriver = new ChromeDriver(chromeOptions);

        this.webDriver.get(url);
    }

    public void quit(){
        this.webDriver.quit();
    }

    public void start() throws InterruptedException {
        Set<String> handles = webDriver.getWindowHandles();
        String[] strings = handles.toArray(new String[0]);
        this.webDriver.switchTo().window(strings[0]);
        Thread.sleep(3000);
        try {
            //获取课程列表
            List<WebElement> ncells = webDriver.findElements(By.cssSelector(".ncells"));
            for (WebElement cell : ncells) {
                WebElement student = cell.findElement(By.cssSelector(".roundpointStudent"));
                //课程已经完成
                if (!"2".equals(student.getText())){
                    continue;
                }
                Actions actions = new Actions(webDriver);

                actions.moveToElement(cell).click(cell).perform();
                Thread.sleep(1000);

                //获取tab页
                WebElement tab = webDriver.findElement(By.cssSelector(".tabtags"));
                List<WebElement> spans = tab.findElements(By.cssSelector("span"));
                for (WebElement span : spans) {
                    String title = span.getAttribute("title");
                    if ("视频".equals(title)){
                        //点击tab页
                        Actions temp = new Actions(webDriver);
                        temp.moveToElement(span).perform();
                        Thread.sleep(500);
                        temp.click(span).perform();
                        Thread.sleep(500);
                        break;
                    }
                }

                /**
                 * 滚动进度条
                 */
                ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0,250);");


                //进入 iframe
                WebElement iframe = webDriver.findElement(By.tagName("iframe"));
                webDriver.switchTo().frame(iframe);
                Thread.sleep(500);
                WebElement iframe2 = webDriver.findElement(By.tagName("iframe"));
                webDriver.switchTo().frame(iframe2);
                //点击播放按钮
                WebElement videoButton = webDriver.findElement(By.cssSelector(".vjs-big-play-button"));
                Actions videoAction = new Actions(webDriver);
                videoAction.moveToElement(videoButton).perform();
                Thread.sleep(500);
                videoAction.click(videoButton).perform();
                Thread.sleep(500);
                //获取视频播放器
                WebElement video = webDriver.findElement(By.cssSelector("#video_html5_api"));

                Thread.sleep(300);
                Dimension videoSize = video.getSize();
                //移动到播放器中间
                videoAction.moveToElement(video,videoSize.getWidth()/2,videoSize.getHeight()/2).perform();
                Thread.sleep(500);
                //暂停
                /*videoAction.click(video).perform();
                Thread.sleep(500);
                //切换二倍速
                List<WebElement> elements = webDriver.findElements(By.cssSelector(".vjs-playback-rate"));
                for (WebElement element : elements) {
                    if ("播放速度".equals(element.getAttribute("title"))){
                        videoAction.moveToElement(element).perform();
                        Thread.sleep(500);
                        List<WebElement> items = webDriver.findElements(By.cssSelector(".vjs-menu-item-text"));
                        for (WebElement item : items) {
                            if ("2x".equals(item.getText())){
                                videoAction.click(item);
                                break;
                            }
                        }
                        break;
                    }
                }
                //移动到播放器中间
                videoAction.moveToElement(video,videoSize.getWidth()/2,videoSize.getHeight()/2).perform();
                Thread.sleep(500);*/
                //播放
                videoAction.click(video).perform();
                Thread.sleep(500);
                //监听滚动跳
                WebElement progress = webDriver.findElement(By.cssSelector(".vjs-progress-holder"));
                String value = progress.getAttribute("aria-valuenow");

                while (!value.startsWith("100")){
                    boolean haveAns = false;
                    //出现答题卡
                    try{
                        WebElement submit = webDriver.findElement(By.cssSelector(".ans-videoquiz-submit"));
                        haveAns = true;
                        WebElement opts = webDriver.findElement(By.cssSelector(".ans-videoquiz-opts"));
                        List<WebElement> labels = opts.findElements(By.tagName("label"));
                        int count = 0;
                        for (WebElement label : labels) {

                            WebElement input = label.findElement(By.tagName("input"));
                            JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
                            if (input.getAttribute("value").equals("true")){
                                Actions temp = new Actions(webDriver);
                                temp.moveToElement(input).perform();
                                Thread.sleep(500);
                                temp = new Actions(webDriver);
                                temp.click(input).perform();
                                Thread.sleep(500);
                                temp = new Actions(webDriver);
                                temp.moveToElement(submit).perform();
                                Thread.sleep(500);
                                temp.click(submit).perform();
                               /* javascriptExecutor.executeAsyncScript("document.querySelectorAll(\"input[name='ans-videoquiz-opt']\")["+count+"].click()");
                                Thread.sleep(500);
                                javascriptExecutor.executeAsyncScript("document.querySelector(\".ans-videoquiz-submit\").click()");
                                Thread.sleep(500);*/
                            }else {
                                count++;
                                continue;
                            }

                            boolean flag =true;
                            while (true){
                                try{
                                    //警告框接受
                                    Alert alert = webDriver.switchTo().alert();
                                    alert.accept();
                                    Thread.sleep(500);
                                    //回答错误
                                    flag = false;
                                }catch (NoAlertPresentException e){
                                    break;
                                }
                            }
                            //回答正确
                            if (flag){
                                break;
                            }
                        }
                    }catch (NoSuchElementException e){
                    }
                    Thread.sleep(500);
                    if (!haveAns){
                        try{
                            //视频被暂停
                            webDriver.findElement(By.cssSelector(".vjs-paused"));
                            videoAction.click(video).perform();
                        }catch (NoSuchElementException e){

                        }
                    }

                    Thread.sleep(1000);
                    value = progress.getAttribute("aria-valuenow");
                }

                //推出iframe
                webDriver.switchTo().defaultContent();
            }
            System.out.println(ncells);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
