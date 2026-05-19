package com.fedstack.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskOrchestratorApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
		// sanity check
	}

	@Test
	void allowsFrontendCorsRequests() throws Exception {
		mockMvc.perform(
						options("/jobs") // any endpoint works
								.header("Origin", "https://orchestrator-dashboard.vercel.app")
								.header("Access-Control-Request-Method", "GET")
				)
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin",
						"https://orchestrator-dashboard.vercel.app"))
				.andExpect(header().exists("Access-Control-Allow-Methods"));
	}

	@Test
	void blocksUnknownOrigins() throws Exception {
		mockMvc.perform(
						options("/jobs")
								.header("Origin", "https://malicious-site.com")
								.header("Access-Control-Request-Method", "GET")
				)
				.andExpect(status().isForbidden());
	}
}
