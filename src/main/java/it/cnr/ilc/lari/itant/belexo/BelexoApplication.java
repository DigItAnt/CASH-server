package it.cnr.ilc.lari.itant.belexo;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BelexoApplication {
    private static final Logger log = LoggerFactory.getLogger(BelexoApplication.class);

	public BelexoApplication() throws Exception {
		log.info("Starting Application");
		JcrManager.start();

		JcrManager.test1();
		JcrManager.test2();

	}

	@PreDestroy
	public void onDestroy() {
		log.info("Stopping Application");
		JcrManager.stop();
	}

	public static void main(String[] args) {
		SpringApplication.run(BelexoApplication.class, args);
	}

}
