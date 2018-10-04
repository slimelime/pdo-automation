package automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

public class WebDriverUtil {
  private int timeOutInSeconds;
  WebDriver driver;

  WebDriverUtil(WebDriver driver, int timeOutInSeconds) {
    this.driver = driver;
    this.timeOutInSeconds = timeOutInSeconds;
  }

  WebDriverWait getWait() {
    return new WebDriverWait(driver, timeOutInSeconds);
  }

  WebDriverWait getWait(int timeOutInSeconds) {
    return new WebDriverWait(driver, timeOutInSeconds);
  }

  void close() {
    if (driver != null) {
      try {
        driver.close();
        driver.quit();
        driver = null;
      } catch (Exception e) {
        //ignore
      }
    }
  }

  private void acceptAlert() throws Exception {
    Thread.sleep(500);
    long expireTime = Calendar.getInstance().getTimeInMillis() + timeOutInSeconds * 1000;

    while(Calendar.getInstance().getTimeInMillis() < expireTime) {
      try {
        driver.switchTo().alert().accept();
        return;
      } catch (Exception e) {
        Thread.sleep(500);
      }
    }
    throw new Exception("there is no alert");
  }

  void click(By locator) {
    getWait().until(d -> {
      try {
        d.findElement(locator).click();
        return true;
      } catch (Exception e) {
        return false;
      }
    });
  }

  void clickUtil(By locator, By verify) {
    getWait().until(d -> {
      try {
        if (d.findElements(verify).size() > 0)
          return true;

        d.findElement(locator).click();
        return false;
      } catch (Exception e) {
        return false;
      }
    });
  }

  void clickUtil(By locator, By verify, int sleepInSeconds) {
    getWait().until(d -> {
      try {
        if (d.findElements(verify).size() > 0)
          return true;

        d.findElement(locator).click();
        Thread.sleep(sleepInSeconds * 1000);
        return false;
      } catch (Exception e) {
        return false;
      }
    });
  }

  String getText(By locator) {
    WebElement element = getElement(locator);

    switch (element.getTagName()) {
      case "input":
        return element.getAttribute("value");
       default:
         return element.getText();
    }
  }

  void switchToNewWindow() throws Exception {
    String currentUrl = driver.getCurrentUrl();

    Thread.sleep(500);
    long expireTime = Calendar.getInstance().getTimeInMillis() + timeOutInSeconds * 1000;

    while(Calendar.getInstance().getTimeInMillis() < expireTime) {
      ArrayList<String> handles = new ArrayList<>();
      handles.addAll(driver.getWindowHandles());

      driver.switchTo().window(handles.get(handles.size()-1));
      if (!driver.getCurrentUrl().equalsIgnoreCase(currentUrl)) return;

      Thread.sleep(500);
    }

    throw new Exception("there is no new window opened");
  }

  void switchToWindowByUrl(String urlToMatch) throws Exception {
    long expireTime = Calendar.getInstance().getTimeInMillis() + timeOutInSeconds * 1000;
    while(Calendar.getInstance().getTimeInMillis() < expireTime) {
      Optional<String> match = driver.getWindowHandles()
          .stream()
          .filter(h -> {
            driver.switchTo().window(h);
            return driver.getCurrentUrl().contains(urlToMatch);
          })
          .findFirst();
      if (match.isPresent()) {
        driver.switchTo().window(match.get());
        return;
      }
      Thread.sleep(500);
    }
    throw new Exception("Can't switch to window:" + urlToMatch);
  }

  void switchToFrame(String idOrName) throws Exception {
    Thread.sleep(500);
    long expireTime = Calendar.getInstance().getTimeInMillis() + timeOutInSeconds * 1000;
    while(Calendar.getInstance().getTimeInMillis() < expireTime) {
      try {
        driver.switchTo().defaultContent();
        driver.switchTo().frame(idOrName);
        return;
      } catch (Exception e) {
        Thread.sleep(500);
      }
    }
    throw new Exception("Can't switch to frame by :" + idOrName);
  }

  WebElement getElement(By locator) {
     return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  void setValue(By locator, String text) {
    getWait().until(d -> {
      try {
        WebElement element = d.findElement(locator);

        if (element.getTagName().equalsIgnoreCase("select")) {
          new Select(element).selectByValue(text);
        } else {
          element.clear();
          element.sendKeys(text);
        }
        return true;
      } catch (Exception e) {
        return false;
      }
    });
  }

  void check(By locator) {
    getWait().until(d -> {
      try {
        WebElement element = d.findElement(locator);

        if (!element.isSelected()) {
          element.click();
        }

        return element.isSelected();
      } catch (Exception e) {
        return false;
      }
    });
  }

  void checkUntil(By locator, By verify) {
    getWait().until(d -> {
      try {
        WebElement element = d.findElement(locator);

        if (!element.isSelected()) {
          element.click();
        } else {
          if (d.findElements(verify).size() > 0 && d.findElements(verify).get(0).isDisplayed()) return true;
          else {
            d.navigate().refresh();
            Thread.sleep(500);
          }
        }
        return false;
      } catch (Exception e) {
        return false;
      }
    });
  }

  void uncheck(By locator) {
    getWait().until(d -> {
      try {
        WebElement element = d.findElement(locator);

        if (element.isSelected()) {
          element.click();
        }

        return !element.isSelected();
      } catch (Exception e) {
        return false;
      }
    });
  }

  void setText(By locator, String text) {
    getWait().until(d -> {
      try {
        WebElement element = d.findElement(locator);

        if (element.getTagName().equalsIgnoreCase("select")) {
            new Select(element).selectByVisibleText(text);
        } else {
          element.clear();
          element.sendKeys(text);
        }
        return true;
      } catch (Exception e) {
        return false;
      }
    });
  }
}
