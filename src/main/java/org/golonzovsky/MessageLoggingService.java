package org.golonzovsky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

/**
 * @author golonzovsky on 10/5/15
 */
@Slf4j
@Service
public class MessageLoggingService {

  public String logMessage(String message){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication == null ? null : ((User) authentication.getPrincipal()).getUsername();
    log.info("name from context = '{}', message = '{}'", username, message);

    return "ok";//returning non-void response to enable blocking gateway call based on aggregator release group strategy
  }
}
