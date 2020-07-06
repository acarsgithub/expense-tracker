package io.expense.expensetracker;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseTrackerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testHome() throws Exception {

		String result = mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		System.out.println("#####################################");
		System.out.println(result);
		Assert.isTrue(result.equals("<h2><center>Welcome! Please log in to update or see your current expenses. " +
				"If you're already logged in, then congratulations!</center></h2>"));
	}

	@Test
	public void testXSS() throws Exception {
		String result = mockMvc.
				perform(get("/all-users?admin-username=<script>alert('XSS!')</script>").accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}

	@Test
	public void testXSS2() throws Exception {
		String result = mockMvc.
				perform(get("/transaction-history/acarary?xss=<script>alert('XSS!')</script>").accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}

	/*
	// STILL NEED TO COMPLETE THIS TEST
	@Test
	public void testSQLInjection() throws Exception {
		String result = mockMvc.
				perform(get("/add-new-account/acarary").accept(MediaType.TEXT_HTML_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType("text/html;charset=UTF-8"))
				.andReturn().getResponse().getContentAsString();
	}
	 */


}
