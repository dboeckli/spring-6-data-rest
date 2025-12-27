package ch.dboeckli.spring.datarest.web.ui;

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

import static ch.dboeckli.spring.datarest.web.BeerWebController.BEER_PAGE;
import static ch.dboeckli.spring.datarest.web.BeerWebController.LIST_BEERS_PAGE;
import static org.junit.jupiter.api.Assertions.*;

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
        webDriver.get("http://localhost:" + port + LIST_BEERS_PAGE);
        waitForPageLoad();
        assertEquals("Beer List", webDriver.getTitle());
    }

    @Test
    @Order(1)
    void testBeerListContainsItems() {
        webDriver.get("http://localhost:" + port + LIST_BEERS_PAGE);
        waitForPageLoad();
        
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        List<WebElement> beerRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#beerTable tbody tr")));
        
        log.info("### Found {} beer rows", beerRows.size());
        
        assertFalse(beerRows.isEmpty(), "Beer list should contain items");
        assertEquals(25, beerRows.size());
    }

    @Test
    void testEditBeer() {
        // Navigate to the beer list page
        webDriver.get("http://localhost:" + port + LIST_BEERS_PAGE);
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
        wait.until(ExpectedConditions.urlContains(BEER_PAGE + "/edit/"));

        // Find the beer name input field and update it
        WebElement beerNameInput = webDriver.findElement(By.id("beerName"));
        String newBeerName = "Updated Beer Name";
        beerNameInput.clear();
        beerNameInput.sendKeys(newBeerName);

        // Submit the form
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        try {
            submitButton.click();
        } catch (Exception e) {
            JavascriptExecutor executor = (JavascriptExecutor) webDriver;
            executor.executeScript("arguments[0].click();", submitButton);
        }

        // Wait for the beer list page to reload
        wait.until(ExpectedConditions.urlToBe("http://localhost:" + port + LIST_BEERS_PAGE));

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

    @Test
    @Order(2)
    void testCreateNewBeer() {
        // Navigate to the beer list page
        webDriver.get("http://localhost:" + port + LIST_BEERS_PAGE);
        waitForPageLoad();

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        // Get the initial total number of beers
        WebElement totalItemsElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("totalItems")));
        int initialTotalItems = Integer.parseInt(totalItemsElement.getText());
        log.info("Initial total items: {}", initialTotalItems);

        // Click on the "Create New Beer" button
        WebElement newBeerButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("createNewBeer")));
        newBeerButton.click();

        // Wait for the new beer form to load
        wait.until(ExpectedConditions.urlContains(BEER_PAGE + "/new"));

        // Fill in the form
        WebElement beerNameInput = webDriver.findElement(By.id("beerName"));
        String newBeerName = "Test New Beer " + System.currentTimeMillis();
        beerNameInput.sendKeys(newBeerName);

        WebElement beerStyleInput = webDriver.findElement(By.id("beerStyle"));
        beerStyleInput.sendKeys("IPA");

        WebElement upcInput = webDriver.findElement(By.id("upc"));
        upcInput.sendKeys("123456789012");

        WebElement priceInput = webDriver.findElement(By.id("price"));
        priceInput.sendKeys("10.99");

        WebElement quantityOnHandInput = webDriver.findElement(By.id("quantityOnHand"));
        quantityOnHandInput.sendKeys("100");

        // Submit the form
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        try {
            submitButton.click();
        } catch (Exception e) {
            JavascriptExecutor executor = (JavascriptExecutor) webDriver;
            executor.executeScript("arguments[0].click();", submitButton);
        }

        // Wait for the beer list page to reload
        wait.until(ExpectedConditions.urlToBe("http://localhost:" + port + LIST_BEERS_PAGE));

        // Check that the total number of beers has increased by 1
        totalItemsElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("totalItems")));
        int finalTotalItems = Integer.parseInt(totalItemsElement.getText());
        log.info("Final total items: {}", finalTotalItems);

        assertEquals(initialTotalItems + 1, finalTotalItems, "Total number of beers should increase by 1 after creation");

        // Navigate to the next page
        WebElement nextPageButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class, 'page-link') and text()='Next']")));
        nextPageButton.click();
        waitForPageLoad();
        
        // Verify that the new beer is in the list
        List<WebElement> beerNames = webDriver.findElements(By.cssSelector("td[id^='beerName-']"));
        boolean newBeerExists = beerNames.stream()
            .anyMatch(element -> element.getText().equals(newBeerName));
        assertTrue(newBeerExists, "Newly created beer should be present in the list");
    }

    @Test
    @Order(99)
    void testDeleteBeer() {
        // Navigate to the beer list page
        webDriver.get("http://localhost:" + port + LIST_BEERS_PAGE);
        waitForPageLoad();

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

        // Get the initial total number of beers
        WebElement totalItemsElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("totalItems")));
        int initialTotalItems = Integer.parseInt(totalItemsElement.getText());
        log.info("Initial total items: {}", initialTotalItems);

        // Find the delete button for the first beer
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[id^='deleteBeer-']")
        ));
        String deleteButtonId = deleteButton.getAttribute("id");
        String beerId = StringUtils.substringAfter(deleteButtonId, "deleteBeer-");

        // Log the beer being deleted
        WebElement beerNameElement = webDriver.findElement(By.id("beerName-" + beerId));
        String beerName = beerNameElement.getText();
        log.info("Deleting beer: ID = {}, Name = {}", beerId, beerName);

        // Click the delete button
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", deleteButton);

        // Handle the confirmation dialog
        wait.until(ExpectedConditions.alertIsPresent());
        webDriver.switchTo().alert().accept();

        // Wait for the page to reload
        waitForPageLoad();

        // Check that the total number of beers has decreased by 1
        totalItemsElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("totalItems")));
        int finalTotalItems = Integer.parseInt(totalItemsElement.getText());
        log.info("Final total items: {}", finalTotalItems);

        assertEquals(initialTotalItems - 1, finalTotalItems, "Total number of beers should decrease by 1 after deletion");

        // Verify that the deleted beer is no longer in the list
        List<WebElement> remainingBeerNames = webDriver.findElements(By.cssSelector("td[id^='beerName-']"));
        boolean beerStillExists = remainingBeerNames.stream()
            .anyMatch(element -> element.getText().equals(beerName));
        assertFalse(beerStillExists, "Deleted beer should not be present in the list");
    }

    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
        wait.until((ExpectedCondition<Boolean>) wd ->
            Objects.equals(((JavascriptExecutor) wd).executeScript("return document.readyState"), "complete"));
    }
}
