package org.golonzovsky;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.ThreadStatePropagationChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;


public class MDCContextPropagationChannelInterceptor
    extends ThreadStatePropagationChannelInterceptor<Map<String, String>> {

  private final static ThreadLocal<Map<String, String>> ORIGINAL_CONTEXT = new ThreadLocal<Map<String, String>>();

  @Override
  public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
    cleanup();
  }

  @Override
  protected Map<String, String> obtainPropagatingContext(Message<?> message, MessageChannel channel) {
    if (!DirectChannel.class.isAssignableFrom(AopUtils.getTargetClass(channel))) {
      return MDC.getCopyOfContextMap();
    }
    return null;
  }

  @Override
  protected void populatePropagatedContext(Map<String, String> mdc, Message<?> message,
                                           MessageChannel channel) {
    if (mdc != null) {
      Map<String, String> currentMDC = MDC.getCopyOfContextMap();

      ORIGINAL_CONTEXT.set(currentMDC);

      MDC.setContextMap(mdc);
    }
  }

  private void cleanup() {
    Map<String, String> originalContext = ORIGINAL_CONTEXT.get();
    try {
      if (originalContext == null || originalContext.isEmpty()) {
        MDC.clear();
        ORIGINAL_CONTEXT.remove();
      }
      else {
        MDC.setContextMap(originalContext);
      }
    }
    catch (Throwable t) {//NOSONAR
      MDC.clear();
    }
  }

}