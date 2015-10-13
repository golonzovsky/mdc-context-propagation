package org.golonzovsky;

import org.springframework.aop.support.AopUtils;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.ThreadStatePropagationChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The {@link ExecutorChannelInterceptor} implementation responsible for
 * the {@link SecurityContext} propagation from one message flow's thread to another
 * through the {@link MessageChannel}s involved in the flow.
 * <p>
 * In addition this interceptor cleans up (restores) the {@link SecurityContext}
 * in the containers Threads for channels like
 * {@link org.springframework.integration.channel.ExecutorChannel}
 * and {@link org.springframework.integration.channel.QueueChannel}.
 * @author Artem Bilan
 * @see ThreadStatePropagationChannelInterceptor
 * @since 4.2
 */
public class SecurityContextPropagationChannelInterceptor
    extends ThreadStatePropagationChannelInterceptor<Authentication> {

  private final static SecurityContext EMPTY_CONTEXT = SecurityContextHolder.createEmptyContext();

  private final static ThreadLocal<SecurityContext> ORIGINAL_CONTEXT = new ThreadLocal<SecurityContext>();

  @Override
  public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
    cleanup();
  }

  @Override
  protected Authentication obtainPropagatingContext(Message<?> message, MessageChannel channel) {
    if (!DirectChannel.class.isAssignableFrom(AopUtils.getTargetClass(channel))) {
      return SecurityContextHolder.getContext().getAuthentication();
    }
    return null;
  }

  @Override
  protected void populatePropagatedContext(Authentication authentication, Message<?> message,
                                           MessageChannel channel) {
    if (authentication != null) {
      SecurityContext currentContext = SecurityContextHolder.getContext();

      ORIGINAL_CONTEXT.set(currentContext);

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
    }
  }

  private void cleanup() {
    SecurityContext originalContext = ORIGINAL_CONTEXT.get();
    try {
      if (originalContext == null || EMPTY_CONTEXT.equals(originalContext)) {
        SecurityContextHolder.clearContext();
        ORIGINAL_CONTEXT.remove();
      }
      else {
        SecurityContextHolder.setContext(originalContext);
      }
    }
    catch (Throwable t) {//NOSONAR
      SecurityContextHolder.clearContext();
    }
  }

}