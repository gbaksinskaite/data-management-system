package test;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import page.AdminNewUserPage;
import page.EditUserPage;
import page.LoginPage;
import page.MainPage;
import page.ProfilePage;
import page.UserListPage;
import utilities.API;
import utilities.GetSessionId;

public class NewUserTests extends AbstractTest {
	LoginPage loginPage;
	MainPage mainPage;
	AdminNewUserPage adminNewUserPage;
	UserListPage userListPage;
	EditUserPage editUserPage;
	ProfilePage profilePage;
	WebDriverWait wait;
	String deleteUserApiURL;
	String deleteGroupApiUrl;
	String sessionID;

	@Parameters({ "groupName" })
	@BeforeClass
	public void preconditions(String groupName) throws IOException {
		loginPage = new LoginPage(driver);
		mainPage = new MainPage(driver);
		adminNewUserPage = new AdminNewUserPage(driver);
		userListPage = new UserListPage(driver);
		editUserPage = new EditUserPage(driver);
		profilePage = new ProfilePage(driver);
		wait = new WebDriverWait(driver, 2);
		deleteUserApiURL = "http://akademijait.vtmc.lt:8180/dvs/api/delete/{username}";
		deleteGroupApiUrl = "http://akademijait.vtmc.lt:8180/dvs/api/group/{groupname}/delete";
		sessionID =  GetSessionId.login("admin", "adminadmin");
		API.createGroup("Some group description", "[]", "[]", groupName, "[]", sessionID);
	}

	@Parameters({ "adminUserName", "adminPassword", "groupName" })
	@BeforeGroups({ "newUserTests", "newUserFieldValidationTests" })
	public void login(String adminUserName, String adminPassword, String groupName) {
		loginPage.sendKeysUserName(adminUserName);
		loginPage.sendKeysPassword(adminPassword);
		loginPage.clickButtonLogin();
	}

	@BeforeMethod
	public void navigateToNewUserPage() {
		mainPage.clickAdminButton();
		mainPage.clickAdminNewUserButton();
		
	}
	
	@AfterGroups("newUserTests")
	public void logout() throws UnirestException {
		mainPage.waitForLogoutButton();
		mainPage.clickLogoutButton();
	}

	@AfterClass
	@Parameters({ "newUserUserName", "newAdminUserName", "groupName" })
	public void deleteEntities(String newUserUserName, String newAdminUserName, String groupName) throws IOException{
	sessionID =  GetSessionId.login("admin", "adminadmin");
	API.deleteUser(newUserUserName,sessionID);	
	API.deleteUser(newAdminUserName, sessionID);
	API.deleteGroup(groupName, sessionID);	
	}

	/*-
	 * Test creates new user with admin role, checks if all properties are saved correctly in user list, 
	 * "Edit user" page and "Profile" page, checks login to the system with new user's credentials.
	 * 
	 * Preconditions: admin is logged in the system, at least one group was created.
	 * 
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username", "Password", 
	 * check "Yes" on "Admin" selection, search for a group name, click button "Add", click button "Create". 
	 * 3. Click "Admin" menu, "Users" option. 
	 * 4. Search for created Username. 
	 * 5. Check if properties ("First Name", "Last Name", "Username", "Role") on a list are displayed correctly . 
	 * 6. Click "Edit / View" button. 
	 * 7. Check if all properties ("First Name", "Last Name", "Username", "Role", groups) are displayed correctly. 
	 * 8. Click button "Cancel". 
	 * 9. Click button "Logout". 
	 * 10. Login to the system using new admin's username and password, click button "Login". 	
	 * 11. Check if all data on Profile Page is displayed correctly.
	 */
	@Parameters({ "newAdminFirstName", "newAdminLastName", "newAdminUserName", "newAdminPassword", "newAdminRole",
			"groupName" })
	@Test(groups = { "newUserTests" }, priority = 1, enabled = true)
	public void createNewAdminTest(String newAdminFirstName, String newAdminLastName, String newAdminUserName,
			String newAdminPassword, String newAdminRole, String groupName) throws InterruptedException {
		 SoftAssert softAssertion= new SoftAssert();
		adminNewUserPage.sendKeysFirstName(newAdminFirstName);
		adminNewUserPage.sendKeysLastName(newAdminLastName);
		adminNewUserPage.sendKeysUserName(newAdminUserName);
		adminNewUserPage.sendKeysPassword(newAdminPassword);
		adminNewUserPage.clickAdminRadio();
		adminNewUserPage.sendKeysSearchGroup(groupName);		
		adminNewUserPage.clickAddRemoveSpecificGroupButton(groupName);
		adminNewUserPage.waitForGroupSelection();
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "New user isn't created");
		driver.navigate().refresh();
		mainPage.clickAdminButton();
		mainPage.clickAdminUsersButton();
		userListPage.sendKeysSearchForUser(newAdminUserName);
		assertTrue(userListPage.getFirstNameFromUserListByUsername(newAdminUserName).equals(newAdminFirstName),
				"Admin's First Name isn't displayed correctly in user list");
		assertTrue(userListPage.getLastNameFromUserListByUsername(newAdminUserName).equals(newAdminLastName),
				"Admin's Last Name isn't displayed correctly in user list");
		assertTrue(userListPage.getRoleFromUserListByUsername(newAdminUserName).equals(newAdminRole),
				"Admin's role isn't displayed correctly in user list");
		userListPage.clickViewEditSpecificUserButton(newAdminUserName);
		editUserPage.waitForEditUserPage();
		assertTrue(editUserPage.getFirstName().equals(newAdminFirstName), "Admin First Name isn't displayed correctly");
		assertTrue(editUserPage.getLastName().equals(newAdminLastName),
				"Admin Last Name isn't displayed correctly in Edit user form");
		assertTrue(editUserPage.isRadioButtonAdminSelected(),
				"Admin's role isn't displayed correctly in Edit user form");	
		editUserPage.sendKeysSearchGroups(groupName);
		assertTrue(editUserPage.isUserAddedToGroup(groupName), "User was not added to the group correctly");
		editUserPage.clickCancelButton();
		mainPage.clickLogoutButton();
		loginPage.sendKeysUserName(newAdminUserName);
		loginPage.sendKeysPassword(newAdminPassword);
		loginPage.clickButtonLogin();
		mainPage.clickProfileButton();
		assertTrue(profilePage.getTextUsername().equals(newAdminUserName),
				"Admin's Username isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextFirstName().equals(newAdminFirstName),
				"Admin's First Name isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextLastName().equals(newAdminLastName),
				"Admin's Last Name isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextRole().equals(newAdminRole),
				"Admin's Role isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextUserGroups().equals(groupName),
				"Admin's group isn't displayed correctly in profile page");
		profilePage.clickButtonClose();
		softAssertion.assertAll();
	}

	/*-
	 * Test creates new user, checks if all properties are saved correctly in user list, 
	 * "Edit user" page and "Profile" page, checks logins to the system with new user's credentials.
	 * 
	 * Precondition: admin is logged in the system, at least one group is created.
	 * 
	 * Test steps: 	 
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username", "Password", search for a group name, click button
	 *    "Add", click button "Create". 
	 * 3. Click "Admin" menu, "Users" option. 
	 * 4. Search for created Username. 
	 * 5. Check if properties ("First Name", "Last Name", "Username", "Role") on a list are displayed correctly . 
	 * 6. Click "Edit / View" button. 
	 * 7. Check if all properties ("First Name", "Last Name", "Username", "Role", groups) are displayed correctly. 
	 * 8. Click button "Cancel".
	 * 9. Click button "Logout". 
	 * 10. Login to the system using new user's username and password, click button "Login".	 
	 * 11. Check if all user data on Profile Page is displayed correctly.
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName", "newUserPassword", "newUserRole",
			"groupName" })
	@Test(groups = { "newUserTests" }, priority = 1, enabled = true)
	public void createNewUserTest(String newUserFirstName, String newUserLastName, String newUserUserName,
			String newUserPassword, String newUserRole, String groupName) {
		
		 SoftAssert softAssertion= new SoftAssert();
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		adminNewUserPage.checkShowPassword();
		adminNewUserPage.sendKeysSearchGroup(groupName);
		adminNewUserPage.clickAddRemoveSpecificGroupButton(groupName);
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "New admin isn't created");
		driver.navigate().refresh();
		mainPage.clickAdminButton();
		mainPage.clickAdminUsersButton();
		userListPage.sendKeysSearchForUser(newUserUserName);
		assertTrue(userListPage.getFirstNameFromUserListByUsername(newUserUserName).equals(newUserFirstName),
				"User's First Name isn't displayed correctly in user list");
		assertTrue(userListPage.getLastNameFromUserListByUsername(newUserUserName).equals(newUserLastName),
				"User's Last Name isn't displayed correctly in user list");
		assertTrue(userListPage.getRoleFromUserListByUsername(newUserUserName).equals(newUserRole),
				"User's role isn't displayed correctly in user list");
		userListPage.clickViewEditSpecificUserButton(newUserUserName);
		editUserPage.waitForEditUserPage();
		assertTrue(editUserPage.getFirstName().equals(newUserFirstName),
				"User's First Name isn't displayed correctly in Edit user page");
		assertTrue(editUserPage.getLastName().equals(newUserLastName),
				"User's Last Name isn't displayed correctly in Edit user page");
		assertTrue(editUserPage.isRadioButtonUserSelected(), "User's role isn't displayed correctly in Edit user page");
		editUserPage.sendKeysSearchGroups(groupName);
		assertTrue(editUserPage.isUserAddedToGroup(groupName), "User was not added to the group correctly");
		editUserPage.clickCancelButton();
		mainPage.clickLogoutButton();
		loginPage.sendKeysUserName(newUserUserName);
		loginPage.sendKeysPassword(newUserPassword);
		loginPage.clickButtonLogin();
		mainPage.clickProfileButton();
		profilePage.waitForHeaderUsernameVisibility();
		assertTrue(profilePage.getTextFirstName().equals(newUserFirstName),
				"User's First Name isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextLastName().equals(newUserLastName),
				"User's Last Name isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextRole().contentEquals(newUserRole),
				"User's Role isn't displayed correctly in profile page");
		assertTrue(profilePage.getTextUserGroups().equals(groupName),
				"User's group isn't shown correctly in profile page");
		profilePage.clickButtonClose();
		mainPage.clickLogoutButton();
		softAssertion.assertAll();
	}

	/*-
	 * Test checks if:
	 *   - First Name field in New User form is mandatory;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "Last Name", "Username", "Password", click button "Create". 
	 */
	@Parameters({ "newUserLastName", "newUserUserName1", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestEmptyFirstNameField(String newUserLastName, String newUserUserName1,
			String newUserPassword) {
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.firstNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "User should not be created without First Name");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - First Name input in New User form cannot be longer than 20 characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name"(21 characters), "Last Name", "Username", "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName21char", "newUserLastName", "newUserUserName1", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestFirstName21char(String newUserFirstName21char, String newUserLastName,
			String newUserUserName1, String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName21char);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.firstNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with First Name longer than 20 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Last Name field in New User form is mandatory;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Username", "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserUserName1", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestEmptyLastNameField(String newUserFirstName, String newUserUserName1,
			String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.lastNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "User should not be created without Last Name");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Last Name input in New User form cannot be longer than 20 characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name"(21 characters), "Username", "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName21char", "newUserUserName1", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestLastName21char(String newUserFirstName, String newUserLastName21char,
			String newUserUserName1, String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName21char);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.lastNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with Last Name longer than 20 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Username input in New User form cannot be shorter than 3 characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username"(3 characters), "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName3char", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestUsername3char(String newUserFirstName, String newUserLastName, String newUserUserName3char,
			String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName3char);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.userNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with UserName shorter than 3 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Username input in New User form cannot be longer than 20 characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username"(21 characters), "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName21char", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestUsername21char(String newUserFirstName, String newUserLastName, String newUserUserName21char,
			String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName21char);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.userNameLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with UserName longer than 21 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Username input in New User form must be unique;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username"(not unique), "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestNotUniqueUsername(String newUserFirstName, String newUserLastName, String newUserUserName,
			String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.userNameUniquenessValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "User should not be created with not unique Username");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Username input in New User form cannot contain special characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username"(with special characters), "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserNameSpecChar", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestUsernameSpecChar(String newUserFirstName, String newUserLastName,
			String newUserUserNameSpecChar, String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserNameSpecChar);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.userNameNoSpecCharValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with Username with special characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Username input in New User form cannot contain space;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username"(with space), "Password", click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserNameContainsSpace", "newUserPassword" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestUsernameWithSpace(String newUserFirstName, String newUserLastName,
			String newUserUserNameContainsSpace, String newUserPassword) {
		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserNameContainsSpace);
		adminNewUserPage.sendKeysPassword(newUserPassword);
		assertTrue(adminNewUserPage.userNameNoSpacesValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(), "User should not be created with Username with space");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Password input in New User form cannot be shorter than 4 characteras;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username", "Password"(3 characters), click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName1", "newUserPassword3char" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestPassword3char(String newUserFirstName, String newUserLastName, String newUserUserName1,
			String newUserPassword3char) {

		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword3char);
		assertTrue(adminNewUserPage.passwordLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with Password shorter than 4 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Password input in New User form cannot be shorter than 4 characters;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username", "Password"(3 characters), click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName1", "newUserPassword21char" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestPassword21char(String newUserFirstName, String newUserLastName, String newUserUserName1,
			String newUserPassword21char) {

		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPassword21char);
		assertTrue(adminNewUserPage.passwordLengthValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with Password longer than 21 characters");
		adminNewUserPage.clickCancelButton();
	}

	/*-
	 * Test checks if:
	 *   - Password input in New User form cannot contain spaces;
	 *   - field length validation message is displayed correctly (attribute "aria-label" value changes to 
	 *   "Condition not met." and cross mark is shown).	 
	 * Precondition: admin is logged in the system.
	 * Test steps:
	 * 1. Click "Admin" menu, "New user" option. 
	 * 2. Fill fields in New User form: "First Name", "Last Name", "Username", "Password"(with spaces), click button "Create". 
	 */
	@Parameters({ "newUserFirstName", "newUserLastName", "newUserUserName1", "newUserPasswordWithSpace" })
	@Test(groups = { "newUserTests", "newUserFieldValidationTests" }, priority = 1, enabled = true)
	public void newUserTestPasswordWithSpace(String newUserFirstName, String newUserLastName, String newUserUserName1,
			String newUserPasswordWithSpace) {

		adminNewUserPage.sendKeysFirstName(newUserFirstName);
		adminNewUserPage.sendKeysLastName(newUserLastName);
		adminNewUserPage.sendKeysUserName(newUserUserName1);
		adminNewUserPage.sendKeysPassword(newUserPasswordWithSpace);
		assertTrue(adminNewUserPage.passwordNoSpacesValidationLabelAttribute().equals("Condition not met."),
				"Validation message isn't displayed correctly");
		adminNewUserPage.clickCreateButton();
		assertTrue(adminNewUserPage.isCreateButtonDisplayed(),
				"User should not be created with Password that contains space");
		adminNewUserPage.clickCancelButton();
	}

}