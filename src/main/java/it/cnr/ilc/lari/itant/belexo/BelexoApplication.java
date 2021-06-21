package it.cnr.ilc.lari.itant.belexo;

import java.io.File;

import javax.annotation.PreDestroy;
import javax.jcr.Repository;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.server.remoting.davex.JcrRemotingServlet;
import org.apache.jackrabbit.servlet.jackrabbit.JackrabbitRepositoryServlet;
import org.apache.jackrabbit.servlet.jackrabbit.StatisticsServlet;
import org.apache.jackrabbit.webdav.jcr.JCRWebdavServerServlet;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class BelexoApplication {
    private static final Logger log = LoggerFactory.getLogger(BelexoApplication.class);

	private static final String PROP_REPOSITORY_HOME = "repository.home";
    private static final String PROP_REPOSITORY_CONFIG = "repository.config";

	public BelexoApplication() throws Exception {
		log.info("Starting Application");
		JcrManager.init();

		//JcrManager.test2(JcrManager.getRepository());

	}

	@PreDestroy
	public void onDestroy() {
		log.info("Stopping Application");
		//JcrManager.stop();
	}

	//@Bean
    public ServletRegistrationBean<JackrabbitRepositoryServlet> repositoryServlet() {
        final JackrabbitRepositoryServlet servlet = new JackrabbitRepositoryServlet();
        final ServletRegistrationBean<JackrabbitRepositoryServlet> regBean = new ServletRegistrationBean<>(servlet);
        regBean.setLoadOnStartup(1);

        // If system property set (e.g, -Drepository.home=jackrabbit-repository),
        // then set the absolute path to the init param.
        String repositoryHome = System.getProperty(PROP_REPOSITORY_HOME, "jackrabbit-repository");
		if (repositoryHome == null)
			repositoryHome = "/tmp/rabbit";
        if (repositoryHome != null && repositoryHome.length() != 0) {
            final File dir = new File(repositoryHome);
            regBean.addInitParameter(PROP_REPOSITORY_HOME, dir.getAbsolutePath());
        }

        // If system property set (e.g, -Drepository.config=repository.xml), add it to init params.
        final String repositoryConfig = System.getProperty(PROP_REPOSITORY_CONFIG);
        if (repositoryConfig != null && repositoryConfig.length() != 0) {
            regBean.addInitParameter(PROP_REPOSITORY_CONFIG, repositoryConfig);
        }

        return regBean;
    }

    @SuppressWarnings("serial")
    //@Bean
    public ServletRegistrationBean<JcrRemotingServlet> jcrWebdavServerServlet() {
        final JcrRemotingServlet servlet = new JcrRemotingServlet() {
            @Override
            protected Repository getRepository() {
                Repository repository = null;
                final RepositoryContext repositoryContext = (RepositoryContext) getServletContext()
                        .getAttribute(RepositoryContext.class.getName());

                if (repositoryContext != null) {
                    repository = repositoryContext.getRepository();
                } else {
                    log.error("RepositoryContext not found.");
                }

                return repository;
            }
        };

        final ServletRegistrationBean<JcrRemotingServlet> regBean = new ServletRegistrationBean<>(servlet, "/server/*");
        regBean.addInitParameter(AbstractWebdavServlet.INIT_PARAM_MISSING_AUTH_MAPPING, "");
        regBean.addInitParameter(JCRWebdavServerServlet.INIT_PARAM_RESOURCE_PATH_PREFIX, "/server");

        return regBean;
    }

    //@Bean
    public ServletRegistrationBean<StatisticsServlet> statisticsServlet() {
        final StatisticsServlet servlet = new StatisticsServlet();
        final ServletRegistrationBean<StatisticsServlet> regBean = new ServletRegistrationBean<>(servlet,
                "/statistics");
        return regBean;
    }

	public static void main(String[] args) {
		SpringApplication.run(BelexoApplication.class, args);
	}


}
