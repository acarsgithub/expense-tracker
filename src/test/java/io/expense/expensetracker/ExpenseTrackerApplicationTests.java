package io.expense.expensetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.expense.expensetracker.models.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseTrackerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	/*
	The test passes perfectly fine and is okay to have like this!
	 */
	@Test
	public void testHome() throws Exception {
		// Tests basic access to homepage is accessible by anyone and that the get request works
		String result = mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	}

	/*
		This works perfectly fine and is how it should be...
	 */
	@Test
	public void testForcesUserLogin() throws Exception {

		// Tests that get request on all-users controller doesn't work properly without anyone logged in
		// Must redirect to login page
		String result = mockMvc.perform(get("/all-users?admin-username=noadmin")
				.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"))
				.andReturn().getResponse().getContentAsString();
	}


	/*
    This test works perfectly fine, the admin should have access to the all-users page
 	*/
	@Test
	@WithMockUser(username="admin",roles={"ADMIN"})
	public void testAdminCredentialsOnAdminController() throws Exception {

		// Tests that we can successfully gain access to all-users controller when logged in as admin
		String result = mockMvc.perform(get("/all-users?admin-username=admin"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}


	/*
		This test works perfectly fine and is okay to do so...
	 */

	@Test
	public void testCreateNewUser() throws Exception {

		// Creating object to store body message key-value pairs
		Object randomObj = new Object() {
			public final String username = "testuser";
			public final String password = "testuserpass";
		};

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(randomObj);

		// Testing post request to create a new user
		String result = mockMvc.perform(MockMvcRequestBuilders.post("/create-new-user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>Created new manager successfully!</center></h2>"));

	}


	/*
		This tests whether you can create a duplicate user or not
		You should not be able to create a duplicate user! However, it is allowing you to do so at this time
		The solution to this has been commented out in the /create-new-user controller

		EDIT: THIS TEST PASSES IN SUCCESS
 	*/
	@Test
	public void testCreateNewDuplicateUser() throws Exception {
		// Creating object to store body message key-value pairs
		Object randomObj = new Object() {
			public final String username = "acarary";
			public final String password = "acararypass";
		};

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(randomObj);

		// Testing post request to create a user that already exists
		String result = mockMvc.perform(MockMvcRequestBuilders.post("/create-new-user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>The manager you are attempting to create already exists!</center></h2>"));
	}


	/*
		This is testing that only the admin can actually access the all-users controller, and should redirect the user
		if they are not an admin (solution to this involves the security configuration changes to be made for this request)

		EDIT: THIS TEST PASSES ON SUCCESS BRANCH
 	*/
	@Test
	@WithMockUser(username="acarary",roles={"USER"})
	public void testAdminNeededForController() throws Exception {

		// Tests that get request on all-users controller doesn't work properly without anyone logged in
		// Must redirect to login page
		String result = mockMvc.perform(get("/all-users?admin-username=acarary")
				.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().is4xxClientError())
				.andReturn().getResponse().getContentAsString();
	}

	/*
		This tests that different users cannot access the same user's account and add and account for them
		It should be an error, but it's successfully adding the account for a different user
		Solution is to check principal and determine the username of the path variable

		EDIT: THIS TEST NOW PASSES FOR SUCCESS
 	*/
	@Test
	@WithMockUser(username="kanywest",roles={"USER"})
	public void testAccessingDifferentUser() throws Exception {

		// Creating object to store body message key-value pairs
		Object randomObj = new Object() {
			public final String category = "Investment";
			public final String amount = "20";
			public final String acc_name = "Hacked";
		};

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(randomObj);
		System.out.println(json);

		// Testing post request to create a new user
		String result = mockMvc.perform(MockMvcRequestBuilders.post("/add-new-account/acarary")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>You cannot access another individual's account!</center></h2>"));

	}


	/*
		The test fails, because the code in home resource for all-users controller needs to be setup to return an error
		if the admin-username isn't a valid username from the database -- fix this directly in the all-users controller
		by returning a bad request response if the admin-username isn't a username from the database directly

		This is testing the XSS injection

		EDIT: THIS TEST NOW PASSES FOR SUCCESS
	*/
	@Test
	@WithMockUser(username="admin",roles={"ADMIN"})
	public void testXSS() throws Exception {

		// Tests XSS JS Injection
		String result = mockMvc.
				perform(get("/all-users?admin-username=<script>alert('XSS!')</script>")
						.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>That username is not valid!</center></h2>"));
	}


	/*
		This tests that if an XSS JS code injection occurs for the transaction history controller, that an error
		is returned, but this test FAILS for now because there is nothing to prevent an XSS

		Solution is to ensure and set up proper guidelines against what the values of user can be within the controller
		source code

		EDIT: THIS TEST NOW PASSES FOR SUCCESS
	 */
	@Test
	@WithMockUser(username="user",roles={"USER"})
	public void testXSS2() throws Exception {

		// Tests XSS JS Injection on transaction-history controller goes through for a logged in user role
		String result = mockMvc.
				perform(get("/transaction-history/user?user=<script>alert('XSS!')</script>")
						.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>That username is not valid!</center></h2>"));
	}


	/*
    This tests whether a different user can access another users transaction history or not -- they should not
    be able to
    As of now, this returns a success if attempted to do so, but it should redirect to login page! This can be
    done using redirectView.setUrl in conjunction with a check on the current principal/ logged in user

    EDIT: THIS TEST NOW PASSES IN SUCCESS
 */
	@Test
	@WithMockUser(username="kanyewest",roles={"USER"})
	public void testDifferentUserAccessingTransactionHistory() throws Exception {

		// Tests that we can successfully not allow different users to access other users accounts
		String result = mockMvc
				.perform(get("/transaction-history/acarary?user=acarary"))
				.andExpect(status()
						.isOk())
				.andReturn().getResponse()
				.getContentAsString();

		Assert.isTrue(result.equals("<h2><center>That username is not valid!</center></h2>"));
	}



	/*
		This tests whether a different user can modify another users account or not -- they should not
		be able to
		As of now, this returns a success if attempted to do so, but it should redirect to login page! This can be
		done using redirectView.setUrl in conjunction with a check on the current principal/ logged in user

		EDIT: THIS TEST NOW PASSES IN SUCCESS
	 */
	@Test
	@WithMockUser(username="kanyewest",roles={"USER"})
	public void testDifferentUserAccessingModifyAccount() throws Exception {

		// Tests that we can successfully not allow different users to access other users accounts
		String result = mockMvc.perform(get("/modify-account/acarary?user=acarary"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Assert.isTrue(result.equals("<h2><center>That username is not valid!</center></h2>"));
	}


	/*
		This test checks for the possibility of a SQL injection to occur for any of the parameters given in json object
		key value pairs --- it should return a 404 error if foul play is detected, but the test fails to do so as of now

		Solution is to insure no SQL code is executable and/or included within the add-new-account json object values
	 */
	@Test
	@WithMockUser(username="acarary",roles={"USER"})
	public void testSQLInjection() throws Exception {

		// Creating object to store body message key-value pairs
		Object randomObj = new Object() {
			public final String category = "Investment";
			public final String amount = "20";
			public final String acc_name = "M2Finance'); INSERT INTO expenses(`username`, `expense_category`, `expense_value`, `expense_acc_name`) VALUES ('acarary', 'Loan', '-10000', 'SQLInjection";
		};

		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writeValueAsString(randomObj);
		System.out.println(json);

		// Testing post request to create a new user
		String result = mockMvc.perform(MockMvcRequestBuilders.post("/add-new-account/acarary")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		Connection conn = null;
		Statement stmt = null;

		// Open connection and execute query
		conn = DriverManager
				.getConnection("jdbc:mysql://localhost:3306/expensetracker?allowMultiQueries=true", "root", "");
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		String checkExpense = "SELECT expense_acc_name FROM expenses WHERE expense_id= '1'";
		ResultSet checker = stmt.executeQuery(checkExpense);

		// Asserting the SQL code was not interpreted as SQL and was instead used as the acc name
		while (checker.next()){
			Assert.isTrue(checker.getString("expense_acc_name").equals("M2Finance'); INSERT INTO expenses(`username`, `expense_category`, `expense_value`, `expense_acc_name`) VALUES ('acarary', 'Loan', '-10000', 'SQLInjection"));

		}

	}


}
