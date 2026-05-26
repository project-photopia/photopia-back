package com.photopia.photopia_back;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"management.health.mail.enabled=false"
})
class PhotopiaBackApplicationTests {

	@Test
	void contextLoads() {
	}

}
