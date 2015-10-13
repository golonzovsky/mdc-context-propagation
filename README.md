# MDC context-propagation
Spring-integration MDC context-propagation proof of concept.  
[`MDCContextPropagationChannelInterceptor`](https://github.com/golonzovsky/context-propagation/blob/master/src/main/java/org/golonzovsky/MDCContextPropagationChannelInterceptor.java) is made same way as `SecurityContextPropagationChannelInterceptor`. With current implementation they cannot be added simultaneously, due to `ThreadStatePropagationChannelInterceptor` specific.

## Manual testing
Using [httpie](httpie.org) for examples.  
Integration tests to follow when solution will be ready.  

### Using no MDC propagation
2 calls `http :8383/async/message1 -a user1:pass` to fill thread pool MDC's,   
and than 1 call to `http :8383/async/message2 -a user2:pass` to show incorrect username in log.  

```
2015-10-13 22:06:04.068  INFO 11964 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test1'
2015-10-13 22:06:04.070  INFO 11964 --- [asyncExecutor-3] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test2'
2015-10-13 22:06:04.074  INFO 11964 --- [asyncExecutor-1] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'message1'
2015-10-13 22:06:05.618  INFO 11964 --- [asyncExecutor-4] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'message1'
2015-10-13 22:06:05.620  INFO 11964 --- [asyncExecutor-5] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test1'
2015-10-13 22:06:05.620  INFO 11964 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test2'
2015-10-13 22:06:36.969  INFO 11964 --- [asyncExecutor-1] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test1'
2015-10-13 22:06:36.969  INFO 11964 --- [asyncExecutor-3] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'message2'
2015-10-13 22:06:36.969  INFO 11964 --- [asyncExecutor-4] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test2'
```

**Result:** all messages have `[user1]` in log entries   
**Expected result:** third call (last 3 entries) should have `[user2]`   

### Using only MDC propagation
2 calls `http :8383/async/message1 -a user1:pass` to fill thread pool MDC's,   
and than 1 call to `http :8383/async/message2 -a user2:pass` to show incorrect username in log.  

```
2015-10-13 22:11:59.758  INFO 12148 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test1'
2015-10-13 22:11:59.760  INFO 12148 --- [asyncExecutor-1] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'message1'
2015-10-13 22:11:59.761  INFO 12148 --- [asyncExecutor-3] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test2'
2015-10-13 22:12:01.487  INFO 12148 --- [asyncExecutor-4] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'message1'
2015-10-13 22:12:01.488  INFO 12148 --- [asyncExecutor-5] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test1'
2015-10-13 22:12:01.490  INFO 12148 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'null', message = 'test2'
2015-10-13 22:12:04.342  INFO 12148 --- [asyncExecutor-1] org.golonzovsky.MessageLoggingService    : [user2] name from context = 'null', message = 'message2'
2015-10-13 22:12:04.343  INFO 12148 --- [asyncExecutor-3] org.golonzovsky.MessageLoggingService    : [user2] name from context = 'null', message = 'test1'
2015-10-13 22:12:04.343  INFO 12148 --- [asyncExecutor-4] org.golonzovsky.MessageLoggingService    : [user2] name from context = 'null', message = 'test2'
```

**Result:** ok

### Using only SecurityContext propagation
1 call `http :8383/async/message1 -a user1:pass` and than 1 call to `http :8383/async/message2 -a user2:pass` to show different usernames.  

```
2015-10-13 22:21:01.484  INFO 12484 --- [asyncExecutor-3] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'user1', message = 'test2'
2015-10-13 22:21:01.483  INFO 12484 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'user1', message = 'test1'
2015-10-13 22:21:01.483  INFO 12484 --- [asyncExecutor-1] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'user1', message = 'message1'
2015-10-13 22:21:03.758  INFO 12484 --- [asyncExecutor-5] org.golonzovsky.MessageLoggingService    : [user2] name from context = 'user2', message = 'test1'
2015-10-13 22:21:03.758  INFO 12484 --- [asyncExecutor-2] org.golonzovsky.MessageLoggingService    : [user1] name from context = 'user2', message = 'test2'
2015-10-13 22:21:03.758  INFO 12484 --- [asyncExecutor-4] org.golonzovsky.MessageLoggingService    : [user2] name from context = 'user2', message = 'message2'
```

**Result:** 'name from context' is populated properly. MDC context is mixed (in [asyncExecutor-2] entries)
