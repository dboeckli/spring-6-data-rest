package guru.springframework.spring6restmvcapi.web.ui;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BeerListPageIT {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Run in headless mode
        webDriver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Test
    @Order(0)
    void testBeerListPageLoads() {
        webDriver.get("http://localhost:" + port + "/web/beers");
        waitForPageLoad();
        assertEquals("Beer List", webDriver.getTitle());
    }

    @Test
    @Order(1)
     void testBeerListContainsItems() {
        webDriver.get("http://localhost:" + port + "/web/beers");
        waitForPageLoad();
        
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        List<WebElement> beerRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#beerTable tbody tr")));
        
        log.info("### Found {} beer rows", beerRows.size());
        
        assertFalse(beerRows.isEmpty(), "Beer list should contain items");
        assertEquals(25, beerRows.size());
    }

    @Test
    public void testEditBeer() {
        // Navigate to the beer list page
        webDriver.get("http://localhost:" + port + "/web/beers");
        waitForPageLoad();

        // Find and click the edit button for the first beer
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[id^='editBeer-']")
        ));
        // Extract the beer ID from the edit button's ID
        String editButtonId = editButton.getAttribute("id");
        String beerId = StringUtils.substringAfter(editButtonId, "editBeer-");
        
        editButton.click();

        // Wait for the edit page to load
        wait.until(ExpectedConditions.urlContains("/web/beer/edit/"));

        // Find the beer name input field and update it
        WebElement beerNameInput = webDriver.findElement(By.id("beerName"));
        String newBeerName = "Updated Beer Name";
        beerNameInput.clear();
        beerNameInput.sendKeys(newBeerName);

        // Submit the form
        WebElement submitButton = webDriver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        // Wait for the beer list page to reload
        wait.until(ExpectedConditions.urlToBe("http://localhost:" + port + "/web/beers"));

        List<WebElement> beerRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#beerTable tbody tr")));
        log.info("Logging all beer names and IDs:");
        HashMap<String, String> beerMap = new HashMap<>();
        for (WebElement row : beerRows) {
            WebElement nameElement = row.findElement(By.cssSelector("td[id^='beerName-']"));
            String id = nameElement.getAttribute("id").replace("beerName-", "");
            String name = nameElement.getText();
            beerMap.put(id, name);
            log.info("#### Beer ID: {}, Name: {}", id, name);
        }
        log.info("### Checking if the beer name has been updated: {}", beerId);
        assertEquals(newBeerName, beerMap.get(beerId), "Beer name should be updated in the beer list page");
    }

    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
        wait.until((ExpectedCondition<Boolean>) wd ->
            Objects.equals(((JavascriptExecutor) wd).executeScript("return document.readyState"), "complete"));
    }
}
