package test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import page.LoginPage;
import page.MainPage;

public class loginTests extends AbstractTest {
	LoginPage loginPage;
	MainPage mainPage;

	@BeforeClass
	public void preconditions() {
		loginPage = new LoginPage(driver);
		mainPage = new MainPage(driver);
	}

	@AfterClass
	public void after() {
		loginPage.clearLoginFields();
	}

	/**-
	 * Preconditions: <br>
	 *   - login page is open;<br>
	 * Test steps:<br>
	 * 1. Enter admin login name and password.<br>
	 * 2. Click login.<br>
	 * Expected results:<br>
	 *    - Admin is logged in the system properly.<br>
	 */
	@Parameters({ "adminUserName", "adminPasswrod" })
	@Test(groups = { "loginTests" }, priority = 2, enabled = true)
	public void adminLoginTest(String p1, String p2) {
		loginPage.clearLoginFields();
		loginPage.sendKeysUserName(p1);
		loginPage.sendKeysPassword(p2);
		loginPage.clickButtonLogin();
		mainPage.waitForAdminButton();
		assertTrue(driver.findElement(By.xpath("//a[contains(text(),'Admin')]")).isDisplayed(),
				"admin dropdown isn't displayed");
		mainPage.clickLogoutButton();
		driver.navigate().back();
		assertTrue(driver.findElement(By.xpath("//button[@type='submit']")).isDisplayed(),
				"You should no be able to navigate back after logging out");
	}

	/**-
	 * Preconditions: <br>
	 *   - login page is open;<br>
	 * Test steps:<br>
	 * 1. Enter user login name and password.<br>
	 * 2. Click login.<br>
	 * Expected results:<br>
	 *    - User is logged in the system properly.<br>
	 */
	@Parameters({ "userUserName", "userPassword" })
	@Test(groups = { "loginTests" }, priority = 3, enabled = true)
	public void userLoginTest(String p1, String p2) {
		loginPage.clearLoginFields();
		loginPage.sendKeysUserName(p1);
		loginPage.sendKeysPassword(p2);
		loginPage.clickButtonLogin();
		mainPage.waitForLogoutButton();
		assertFalse(driver.findElements(By.xpath("//a[contains(text(),'Admin')]")).size() == 1,
				"admin dropdown should not be displayed");
		mainPage.clickLogoutButton();
		driver.navigate().back();
		assertTrue(driver.findElement(By.xpath("//button[@type='submit']")).isDisplayed(),
				"You should not be able to navigate back after logging out");
	}

	/**-
	 * Preconditions: <br>
	 *   - login page is open;<br>
	 * Test steps:<br>
	 * 1. Leave login name and password fields empty.<br>
	 * 2. Click login.<br>
	 * Expected results:<br>
	 *    - "Incorrect Username or Password!" message is displayed.<br>
	 */
	@Test(groups = { "loginTests" }, priority = 1, enabled = true)
	public void emptyLoginFieldsTest() {
		loginPage.clearLoginFields();
		loginPage.waitForLoginButton();
		loginPage.clickButtonLogin();
		loginPage.waitForWrongLoginText();
		assertTrue(
				driver.findElement(By.xpath("//*[contains(text(), 'Incorrect Username or Password!')]")).isDisplayed(),
				"Incorrect Username or Password! text is not displayed");
	}

	/**-
	 * Preconditions: <br>
	 *   - login page is open;<br>
	 * Test steps:<br>
	 * 1. Enter wrong user login name and password.<br>
	 * 2. Click login.<br>
	 * Expected results:<br>
	 *    - "Incorrect Username or Password!" message is displayed.<br>
	 */
	@Parameters({ "userNameWrong", "passwordWrong" })
	@Test(groups = { "loginTests" }, priority = 4, enabled = true)
	public void wrongLoginTest(String p1, String p2) {
		loginPage.clearLoginFields();
		loginPage.waitForLoginButton();
		loginPage.sendKeysUserName(p1);
		loginPage.sendKeysPassword(p2);
		loginPage.clickButtonLogin();
		loginPage.waitForWrongLoginText();
		assertTrue(
				driver.findElement(By.xpath("//*[contains(text(), 'Incorrect Username or Password!')]")).isDisplayed(),
				"Incorrect Username or Password! text is not displayed");
	}
}
