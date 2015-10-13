package org.golonzovsky;

import java.util.Arrays;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ImportResource("itineraryPricingFlowGatewayContext.xml")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}

@Configuration
@EnableWebSecurity
class SecurityConfig {
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth
        .inMemoryAuthentication()
        .withUser("user1").password("pass").roles("USER")
        .and()
        .withUser("user2").password("pass").roles("USER");
  }
}

@RestController
class ContactRestController {
  @Autowired MessageLoggingService messageLoggingService;
  @Autowired AsyncProcessingGateway pricingGateway;

  @RequestMapping("/sync/{packageId}")
  String processSync(@PathVariable String packageId) {
    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    MDC.put("username", principal.getUsername());
    messageLoggingService.logMessage(packageId);
    return "ok sync";
  }

  @RequestMapping("/async/{packageId}")
  String processAsync(@PathVariable String packageId) {
    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    MDC.put("username", principal.getUsername());
    pricingGateway.logMessageAsync(Arrays.asList(packageId, "test1", "test2"));
    return "ok async";
  }
}