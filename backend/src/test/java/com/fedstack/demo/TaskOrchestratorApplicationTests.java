package com.fedstack.demo;

import com.fedstack.demo.config.CorsConfig;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskOrchestratorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void allowsFrontendCorsRequests() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/jobs");
		request.addHeader("Origin", "http://localhost:5173");
		request.addHeader("Access-Control-Request-Method", "GET");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new CorsConfig().corsFilter().doFilter(request, response, new MockFilterChain());

		assertEquals(200, response.getStatus());
		assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
	}

}
