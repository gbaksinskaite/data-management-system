package lt.vtmc.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import lt.vtmc.documents.service.DocumentService;
import lt.vtmc.groups.service.GroupService;
import lt.vtmc.security.SecurityEntryPoint;
import lt.vtmc.user.controller.UserController;
import lt.vtmc.user.model.User;
import lt.vtmc.user.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SecurityEntryPoint securityEntryPoint;

	@MockBean
	private UserService userService;

	@MockBean
	private User user;

	@MockBean
	private GroupService groupService;

	@MockBean
	private DocumentService docService;

	@Test
	public void testCreateUser() throws Exception {
		User mockUser = new User("testUsername", "testName", "testSurname", "testing123", "USER");

		// userService.createUser to respond back with mockUser
		Mockito.when(userService.createUser(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(mockUser);

		String exampleCreateUserCommand = "{\"username\":\"testAdmin\",\"name\":\"testing123\",\"surname\":\"testing123\",\"password\":\"testing123\",\"names\":[\"dummy1\"]}";

		// Send CreateUserCommand as body to /api/createuser
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/createuser")
				.accept(MediaType.APPLICATION_JSON).content(exampleCreateUserCommand)
				.contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void testCreateAdminUser() throws Exception {
		User mockUser = new User("testUsername", "testName", "testSurname", "testing123", "ADMIN");

		// userService.createSystemAdministrator to respond back with mockUser
		Mockito.when(userService.createSystemAdministrator(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(mockUser);

		String exampleCreateUserCommand = "{\"username\":\"testAdmin\",\"name\":\"testing123\",\"surname\":\"testing123\",\"password\":\"testing123\",\"names\":[\"dummy1\"]}";

		// Send CreateUserCommand as body to /api/createadmin
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/createadmin")
				.accept(MediaType.APPLICATION_JSON).content(exampleCreateUserCommand)
				.contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
}
