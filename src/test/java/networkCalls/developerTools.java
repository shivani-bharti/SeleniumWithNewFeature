package networkCalls;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Console;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.inspector.Inspector;
import org.openqa.selenium.devtools.network.Network;
import org.openqa.selenium.devtools.network.model.BlockedReason;
import org.openqa.selenium.devtools.network.model.ConnectionType;
import org.openqa.selenium.devtools.network.model.ResourceType;
import org.openqa.selenium.devtools.security.Security;
import org.openqa.selenium.devtools.target.Target;
import org.openqa.selenium.devtools.target.model.TargetInfo;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.github.bonigarcia.wdm.WebDriverManager;
//import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.devtools.inspector.Inspector.detached;
import static org.openqa.selenium.devtools.network.Network.emulateNetworkConditions;
import static org.openqa.selenium.devtools.network.Network.loadingFailed;
import static org.openqa.selenium.devtools.target.Target.attachToTarget;
import static org.testng.Assert.assertNotNull;



public class developerTools {
	
	WebDriver driver;
	DevTools devtools;
	@BeforeMethod()
	public void init() {
		WebDriverManager.chromedriver().setup();
		driver =new ChromeDriver();
	
		driver.manage().timeouts().implicitlyWait(6,TimeUnit.SECONDS);

	    devtools = ((ChromeDriver) driver).getDevTools();
	    devtools.createSession();	
	}
	@Test
	public void geoLocation() {
	
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("latitude", 50.2334);
	    params.put("longitude", 0.2334);
	    params.put("accuracy", 1);
	    ((ChromeDriver) driver).executeCdpCommand("Emulation.setGeolocationOverride", params);
		driver.get("https://www.google.com/maps");
	}

	@Test
	public void geoNetworkCalls() {
	devtools.send(Network.enable(Optional.of(1000), Optional.of(100), Optional.of(100)));
	devtools.addListener(Network.responseReceived(),responseReceived -> Assert.assertNull(responseReceived));
	driver.get("https://apache.org");

		}
	
	@Test
	public void enableNetworkOnline() {
		devtools.send(Network.enable(Optional.of(1000), Optional.of(100), Optional.of(100)));
		 devtools.send(emulateNetworkConditions(false, 100, 5000, 2000,
	                Optional.of(ConnectionType.cellular4g)));
		 driver.get("https://https://www.facebook.com");
	}
	@Test
    public void addCustomHeaders() {

        //enable Network
		devtools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        //set custom header
		devtools.send(Network.setExtraHTTPHeaders(ImmutableMap.of("customHeaderName", "customHeaderValue")));

		devtools.addListener(Network.requestWillBeSent(), requestWillBeSent -> Assert
                .assertEquals(requestWillBeSent.getRequest().getHeaders().get("customHeaderName"),
                        "customHeaderValue"));

        driver.get("https://apache.org");
    }
	

    @Test
    public void verifyConsoleMessageAdded() {

        String consoleMessage = "Hello Selenium 4";
        devtools.send(Console.enable());

        //add listener to verify the console message
        devtools.addListener(Console.messageAdded(), consoleMessageFromDevTools ->
                assertEquals(true, consoleMessageFromDevTools.getText().equals("TEXT")));

    }

    @Test
    public void loadInsecureWebsite() {

        //enable Security
    	devtools.send(Security.enable());

        //set ignore certificate errors
    	devtools.send(Security.setIgnoreCertificateErrors(true));

        //load insecure website
        driver.get("https://expired.badssl.com/");

        //verify that the page was loaded
        Assert.assertEquals(true, driver.getPageSource().contains("expired"));

    }
	@AfterMethod
	public void tearDown() {
	
		driver.quit();
	}
}

