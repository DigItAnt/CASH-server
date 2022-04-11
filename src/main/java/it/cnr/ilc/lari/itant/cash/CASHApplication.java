package it.cnr.ilc.lari.itant.cash;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@SpringBootApplication
public class CASHApplication {
    private static final Logger log = LoggerFactory.getLogger(CASHApplication.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

	public CASHApplication() throws Exception {
		log.info("Starting Application");
	}

    @PostConstruct
    void postInit() throws Exception {
        //int result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        //System.out.println("sssssssssssssssssssssssssssssssssssssss  " + result);

        DBManager.init();
     }

	@PreDestroy
	public void onDestroy() {
		log.info("Stopping Application");
	}

	public static void main(String[] args) {
		SpringApplication.run(CASHApplication.class, args);
	}


}
