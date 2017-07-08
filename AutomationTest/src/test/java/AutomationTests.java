import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bluesands92 on 07/07/2017.
 */

public class AutomationTests {

    private final static String EMAIL = "alex@bluesands.com";
    private final static String PASSWORD = "iuhgfjnvj134!";
    private final static String CONTINUE_SHOPPING_XPATH = "//*[@title='Continue shopping']";
    private final static String SUBMIT_XPATH = "//*[@id=\"add_to_cart\"]/button";
    private final static String MESSAGE = "the quick brown fox jumped iver the lazy dog";

    private List<Map>  basket = new ArrayList<Map>();
    private String orderReference = "";
    WebDriver driver = null;
    WebDriverWait wait = null;


    /**
     * Test	1:	Happy	path,	purchase	2	items
     1. ‘Quick	view’	an	item
     2. Change	the	size	of	the	item
     3. Add	that	item	to	your	basket
     4. Continue	shopping
     5. ‘Quick	view’	a	different	item	(leave	the	size	at	the	default)
     6. Add	that	item	to	your	basket
     7. View	the	basket	and	confirm	that	the	items	are	of	the	size	you	selected, that	their
     prices	are	correct,	that	Total	Products	is	the	sum	of	the	two	items	and	that	‘Total’
     equals	the	Total	Products	+	Shipping.
     8. Proceed	through	checkout	to	payment	(complete	by	wire)
     */
    @Test(priority = 1)
    public void testOne_HappyPath() throws InterruptedException {

        //goto dresses page, this could be randomised
        driver.get("http://automationpractice.com/index.php?id_category=8&controller=category");

        //get list of all quick-view clickables, could be switched between mobile or desktop
        List<WebElement> products = new ArrayList();
        for (WebElement a : driver.findElement(By.id("center_column")).findElements(By.tagName("a"))){
            if (a.getAttribute("class").equals("quick-view-mobile")){
                products.add(a);
            }
        }

        //Block to add item with default values
        products.get(0).click();
        driver.switchTo().frame(driver.findElement(By.tagName("iframe")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SUBMIT_XPATH)));
        String reference = driver.findElement(By.id("product_reference")).findElement(By.tagName("span")).getText();
        driver.findElement(By.xpath(SUBMIT_XPATH)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath(CONTINUE_SHOPPING_XPATH))));
        basket.add(saveItemDetails(driver, reference));
        driver.findElement(By.xpath(CONTINUE_SHOPPING_XPATH)).click();

        //block to add item with none default size
        products.get(3).click();
        driver.switchTo().frame(driver.findElement(By.tagName("iframe")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SUBMIT_XPATH)));
        reference = driver.findElement(By.id("product_reference")).findElement(By.tagName("span")).getText();
        Select select = new Select(driver.findElement(By.tagName("select")));
        select.selectByIndex(select.getOptions().size()-1);
        driver.findElement(By.xpath(SUBMIT_XPATH)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath(CONTINUE_SHOPPING_XPATH))));
        basket.add(saveItemDetails(driver, reference));
        driver.findElement(By.xpath(CONTINUE_SHOPPING_XPATH)).click();

        //check products are correct
        double expectedTotal = 0;
        driver.get("http://automationpractice.com/index.php?controller=order");
        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> cart = table.findElements(By.tagName("tr"));
        for (WebElement item : cart){
            for(Map<String, String> brought : basket){
                if(item.findElements(By.tagName("small")).get(0).getText().contains(brought.get("product-reference"))){
                    String description = item.findElements(By.tagName("small")).get(1).getText();
                    //name
                    //colour & size
                    if(description.split(", ")[0].toLowerCase().contains("color")){
                        Assert.assertEquals(description.split(", ")[0].split(" : ")[1], brought.get("product-colour"));
                        Assert.assertEquals(description.split(", ")[1].split(" : ")[1], brought.get("product-size"));
                    } else {
                        Assert.assertEquals(description.split(", ")[1].split(" : ")[1], brought.get("product-colour"));
                        Assert.assertEquals(description.split(", ")[0].split(" : ")[1], brought.get("product-size"));
                    }
                    //check unit priceprice
                    String price = item.findElements(By.tagName("td")).get(3)
                            .findElements(By.tagName("span")).get(1).getText();
                    double ex = Double.parseDouble(brought.get("product-price"));
                    double ac = Double.parseDouble(price.substring(1));
                    Assert.assertEquals(ac, ex);

                    //update totals
                    expectedTotal += ex;
                    break;
                }
            }

            //check total price of items and total price including shipping is correct
            if(item.getAttribute("class").contains("last_item")){
                WebElement totals = driver.findElement(By.tagName("tfoot"));
                String total_product = totals.findElement(By.id("total_product")).getText().substring(1);
                Assert.assertEquals(Double.parseDouble(total_product), expectedTotal);
                String total_shipping = totals.findElement(By.id("total_shipping")).getText().substring(1);
                String total_price = totals.findElement(By.id("total_price")).getText().substring(1);
                Assert.assertEquals(Double.parseDouble(total_price), expectedTotal + Double.parseDouble(total_shipping));
                break;
            }

        }

        //Proceed through checkout steps and pay by wire
        driver.findElement(By.xpath("//*[@id=\"center_column\"]/p[2]/a[1]/span")).click();
        driver.findElement(By.xpath("//*[@id=\"center_column\"]/form/p/button/span")).click();
        driver.findElement(By.xpath("//*[@id=\"cgv\"]")).click();
        driver.findElement(By.xpath("//*[@id=\"form\"]/p/button/span")).click();
        driver.findElement(By.xpath("//*[@id=\"HOOK_PAYMENT\"]/div[1]/div/p/a")).click();
        driver.findElement(By.xpath("//*[@id=\"cart_navigation\"]/button/span")).click();

        //Obtain order reference for next test case, this way is ugly would look to improve, use regex?
        orderReference = driver.findElement(By.xpath("//*[@id=\"center_column\"]/div"))
                .getText().split("\\n")[6]
                .split(" ")[9];

    }



    /**
     * TEST WILL NOT RUN IF TEST ONE FAILS
     * Test	2:	Review	previous	orders and	add	a	message
     1. View	previous	orders
     2. Select	an	item	from	your previous	order	(there	will	be	others	– confirm	this	by
     date/time)	and	add	a	comment
     3. Confirm	that	the	comment	appears	under	‘messages’

     */
    @Test(dependsOnMethods = { "testOne_HappyPath" })
    public void testTwo_ReviewPreviousOrder() throws InterruptedException {

        //get order history page
        driver.get("http://automationpractice.com/index.php?controller=history");

        //get orders in table, and search for order from test one, click reference to load details
        List<WebElement> orders = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
        for (WebElement order : orders){
            WebElement order_reference = order.findElements(By.tagName("td")).get(0).findElement(By.tagName("a"));
            if(order_reference.getText().equalsIgnoreCase(orderReference)){
                order_reference.click();
                break;
            }
        }

        //add message to the order
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath("//*[@id=\"sendOrderMessage\"]/p[2]/select"))));
        Select select = new Select(driver.findElement(By.xpath("//*[@id=\"sendOrderMessage\"]/p[2]/select")));
        select.selectByIndex(select.getOptions().size()-1);
        driver.findElement(By.name("msgText")).sendKeys(MESSAGE);
        driver.findElement(By.xpath("//*[@id=\"sendOrderMessage\"]/div/button")).click();

        //Verify that order has been updated
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath("//*[@id=\"block-order-detail\"]/div[5]/table/tbody"))));
        WebElement message = driver.findElement(By.xpath("//*[@id=\"block-order-detail\"]/div[5]/table/tbody")).findElements(By.tagName("tr")).get(0);
        Assert.assertEquals(message.findElements(By.tagName("td")).get(1).getText(), MESSAGE);

    }

    /**
     * Test	3:	Capture	images
     1. From	Test
     2	create	an	assertion	which	will	cause	a	fail	(e.g.	confirm	the	dress	is
     red	when	in	fact	it	is	blue)	and	capture	a	screen-grab	on	fail	using	Selenium
     */
    @Test()
    public void testThree_CaptureImage() throws IOException {

        driver.get("http://automationpractice.com/index.php?id_category=8&controller=category");
        List<WebElement> products = new ArrayList();

        for (WebElement a : driver.findElement(By.id("center_column")).findElements(By.tagName("a"))) {
            if (a.getAttribute("class").equals("quick-view-mobile")) {
                products.add(a);
            }
        }

        //block to add item with none default size
        products.get(2).click();
        driver.switchTo().frame(driver.findElement(By.tagName("iframe")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SUBMIT_XPATH)));
        String reference = driver.findElement(By.id("product_reference")).findElement(By.tagName("span")).getText();
        driver.findElement(By.xpath(SUBMIT_XPATH)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath(CONTINUE_SHOPPING_XPATH))));
        basket.add(saveItemDetails(driver, reference));
        driver.findElement(By.xpath(CONTINUE_SHOPPING_XPATH)).click();

        //check products are correct, code recycled from test one
        driver.get("http://automationpractice.com/index.php?controller=order");
        WebElement table = driver.findElement(By.tagName("tbody"));
        List<WebElement> cart = table.findElements(By.tagName("tr"));
        for (WebElement item : cart) {
            for (Map<String, String> brought : basket) {
                if (item.findElements(By.tagName("small")).get(0).getText().contains(brought.get("product-reference"))) {
                    String description = item.findElements(By.tagName("small")).get(1).getText();
                    try {
                        if (description.split(", ")[0].toLowerCase().contains("color")) {
                            //will fail
                            Assert.assertEquals(description.split(", ")[0].split(" : ")[1], "fobar");
                        }
                    //catch Assertion error, take screenshot, update error message and send it
                    }catch (AssertionError assertionError){
                            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                            FileUtils.copyFile(scrFile, new File("/Users/frspare/Desktop/" + scrFile.getName()));
                            throw new AssertionError("A assertion error has been captured," +
                                    "\n a screen shot has been saved \"/Users/frspare/Desktop//screenshot.png\"\n" + assertionError);
                        }
                    break;
                }
            }
        }
    }

    //sets up the driver, webdriver location is hard coded, should be dynamic for best practice
    @BeforeSuite
    public void setup(){
        System.setProperty("webdriver.chrome.driver", "/Users/frspare/IdeaProjects/bsjjAutomationTest/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 30);

    }
    //logs the user in done before each test
    @BeforeMethod
    public void login(){
        driver.get("http://automationpractice.com/index.php?controller=authentication&back=my-account");
        driver.findElement(By.id("email")).sendKeys(EMAIL);
        driver.findElement(By.id("passwd")).sendKeys(PASSWORD);
        driver.findElement(By.id("SubmitLogin")).click();

    }

    //logs the user out done after each test
    @AfterMethod
    public void logout(){
        driver.get("http://automationpractice.com/index.php?mylogout=");

    }

    //closes the drivers session, so no browser is left open.  This could be done post test case too.
    @AfterSuite
    public void cleanup(){
        driver.close();

    }

    private Map<String, String> saveItemDetails(WebDriver driver, String reference){
        Map<String, String> product = new HashMap<String, String>();

        product.put("product-reference", reference);
        product.put("product-name", driver.findElement(By.id("layer_cart_product_title")).getText());
        product.put("product-colour", driver.findElement(By.id("layer_cart_product_attributes")).getText().split(", ")[0]);
        product.put("product-size", driver.findElement(By.id("layer_cart_product_attributes")).getText().split(", ")[1]);
        product.put("product-price", driver.findElement(By.id("layer_cart_product_price")).getText().substring(1));

        return product;
    }

}
