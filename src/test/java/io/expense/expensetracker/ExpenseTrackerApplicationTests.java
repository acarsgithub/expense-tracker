package io.expense.expensetracker;

import io.expense.expensetracker.models.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import java.security.Principal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseTrackerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testHome() throws Exception {

		// Tests basic access to homepage is accessible by anyone and that the get request works
		String result = mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	@WithMockUser(username="admin",roles={"ADMIN"})
	public void testXSS() throws Exception {

		// Tests XSS JS Injection on all-users is able to go through, but only if admin is logged in
		String result = mockMvc.
				perform(get("/all-users?admin-username=<script>alert('XSS!')</script>").accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	@WithMockUser(username="user",roles={"USER"})
	public void testXSS2() throws Exception {

		// Tests XSS JS Injection on transaction-history controller goes through for a logged in user role
		String result = mockMvc.
				perform(get("/transaction-history/user?user=<script>alert('XSS!')</script>")
						.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}


	@Test
	@WithMockUser(username="user",roles={"USER"})
	public void testSQLInjection() throws Exception {

		// Tests SQL Injection successfully occurs on add-new-account controller when user is logged in
		String result = mockMvc.
				perform(get("/add-new-account/user?category=Investment&amount=1&acc_name=M2Finance" +
						"%27%29%3B+INSERT+INTO+expenses%28%60username%60%2C+%60expense_category%60%2C+%60expense_value" +
						"%60%2C+%60expense_acc_name%60%29+VALUES+%28%27acarary%27%2C+%27Loan%27%2C+%27-10000%27%2C+%27SQLInjection")
						.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}


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


	@Test
	@WithMockUser(username="user",roles={"USER"})
	public void testRedirectWhenNotAdmin() throws Exception {

		// Tests that get request on all-users controller doesn't work properly when user is logged in
		// Must redirect to login page
		String result = mockMvc.perform(get("/all-users?admin-username=user")
				.accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().is4xxClientError())
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	@WithMockUser(username="admin",roles={"ADMIN"})
	public void testAdminCredentialsOnAdminController() throws Exception {

		// Tests that we can successfully gain access to all-users controller when logged in as admin
		String result = mockMvc.perform(get("/all-users?admin-username=admin"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}


	@Test
	@WithMockUser(username="admin",roles={"ADMIN"})
	public void testAdminCredentialsOnUserController() throws Exception {

		// Tests that we can successfully gain access to transaction history controller of
		// another user's account when logged in as admin
		String result = mockMvc.perform(get("/transaction-history/acarary?user=Admin"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/plain;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}



}
