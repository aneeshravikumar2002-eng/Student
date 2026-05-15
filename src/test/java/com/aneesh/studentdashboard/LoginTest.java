package com.aneesh.studentdashboard;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class LoginTest {

    @Test
    public void loginTest() throws InterruptedException {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.setBinary("/usr/bin/google-chrome");
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        driver.get("http://15.207.10.227:8000");

        WebElement username =
                driver.findElement(By.xpath("//input[@type='text']"));

        username.sendKeys("aneesh");

        WebElement password =
                driver.findElement(By.xpath("//input[@type='password']"));

        password.sendKeys("12345");

        driver.findElement(By.tagName("button")).click();

        Thread.sleep(3000);

        System.out.println("Dashboard Opened Successfully");

        driver.quit();
    }
}