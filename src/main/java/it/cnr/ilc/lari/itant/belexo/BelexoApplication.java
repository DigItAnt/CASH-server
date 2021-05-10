package it.cnr.ilc.lari.itant.belexo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BelexoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BelexoApplication.class, args);
	}

	@GetMapping("/rest/ping")
	String ping() {
		return "pong";
	}

}
