package com.photopia.photopia_back;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest(properties = {
		"spring.mail.username=test@example.com",
		"spring.mail.password=password",
		"spring.mail.host=localhost",
		"spring.mail.port=587",
		"spring.mail.properties.mail.smtp.starttls.enable=true",
		"spring.mail.properties.mail.smtp.auth=true",
		"management.health.mail.enabled=false"
})
class PhotopiaBackApplicationTests {

	@MockBean
	private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
