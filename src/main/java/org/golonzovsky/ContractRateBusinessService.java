package org.golonzovsky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

/**
 * @author golonzovsky on 10/5/15
 */
@Service("contractRateBusinessService")
@Slf4j
public class ContractRateBusinessService {

  public String searchRates(String packageId){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication == null ? null : ((User) authentication.getPrincipal()).getUsername();
    log.info("name '{}', package '{}'", username, packageId);
    return packageId;
  }
}
