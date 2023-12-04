package it.cnr.ilc.lari.itant.cash.security;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import it.cnr.ilc.lari.itant.cash.DBManager;

// https://www.baeldung.com/spring-boot-keycloak

@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired
  public void configureGlobal(
      AuthenticationManagerBuilder auth) throws Exception {

    KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
        new SimpleAuthorityMapper());
    auth.authenticationProvider(keycloakAuthenticationProvider);
  }

  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(
        new SessionRegistryImpl());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);

    // read "NOSECURITY" env variable
    String authPattern = "/api/**";
    String noSecurityEnv = System.getProperty("NOSECURITY");
    if (noSecurityEnv != null && noSecurityEnv.equals("true")) {
      authPattern = "xxxxxxxxx";
      log.warn("NO SECURITY ENABLED, no API will be protected");
    }

    http.csrf().disable()
        .cors().and()
        .authorizeRequests()
        .antMatchers("/api/public/**").permitAll()
        .antMatchers(authPattern).authenticated()
        // .hasRole("user")
        .anyRequest().permitAll();
  }

}
