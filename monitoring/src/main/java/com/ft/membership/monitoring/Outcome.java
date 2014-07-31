package com.ft.membership.monitoring;

import com.ft.membership.domain.UserId;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Outcome {

    public enum DomainObjectKey {
        UserId("userId"),
        UserEmail("userEmail"),
        ErightsId("erightsId"),
        ErightsGroupId("erightsGroupId");

        private final String key;

        DomainObjectKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static Operation operation(final String operation) {
        return new Operation(operation);
    }
    
    public static class Operation extends Parameters implements AutoCloseable {

        private final String operation;
        private boolean terminated;
        private Object actorOrLogger;

        public Operation(final String operation) {
            checkNotNull(operation, "require operation");
            this.operation = operation;
        }
        
        public Operation with(final UserId id) {
            putNoWrap(DomainObjectKey.UserId.getKey(), id);
            return this;
        }

        public Operation with(final DomainObjectKey key, final Object value) {
            return with(key.getKey(),value);
        }

        public Operation with(final String key, final Object value) {
            putWrapped(key, value);
            return this;
        }

        public Operation started(final Object actorOrLogger) {
            this.actorOrLogger = actorOrLogger;
            new LogFormatter(actorOrLogger).log(this);
            return this;
        }

        public Yield wasSuccessful() {
            return new Yield(this);
        }

        public Failure wasFailure() {
            return new Failure(this);
        }

        protected void terminated() {
            this.terminated = true;
        }

        protected String getName() {
            return operation;
        }

        @Override
        public void close() {
            if(!terminated) {
                this.wasFailure()
                        .throwingException(new RuntimeException("operation auto-closed")) // so we at least get a stack-trace
                        .log(Optional.fromNullable(actorOrLogger).or(this));
            }
        }
    }

    public static interface LoggingTerminal {
        public void log(Object actorOrLogger);
    }

    public static class Yield extends Parameters implements LoggingTerminal {
        private final Operation operation;

        public Yield(final Operation operation) {
            this.operation = operation;
        }

        public Yield yielding(final UserId userId) {
            putNoWrap("userId", userId);
            return this;
        }

        public Yield yielding(final String key, final Object value) {
            putWrapped(key, value);
            return this;
        }

        public Yield yielding(final DomainObjectKey key, final Object value) {
            putWrapped(key.getKey(), value);
            return this;
        }

        public void log(final Object actorOrLogger) {
            new LogFormatter(actorOrLogger).log(operation, this);
        }
    }

    public static class Failure extends Parameters implements LoggingTerminal {
        private Operation operation;
        private Exception thrown;

        public Failure(final Operation operation) {
            this.operation = operation;
        }

        public Failure throwingException(final Exception e) {
            this.thrown = checkNotNull(e, "require exception");
            return this;
        }

        public Failure withMessage(final String message) {
            putWrapped("errorMessage", message);
            return this;
        }

        public Failure withDetail(final String key, final Object detail) {
            putWrapped(key,detail);
            return this;
        }

        public boolean didThrow() {
            return thrown != null;
        }

        public Exception getThrown() {
            return thrown;
        }

        public void log(Object actorOrLogger) {
            new LogFormatter(actorOrLogger).log(operation, this);
        }
    }

    protected static class Parameters {
        private Map<String,Object> params = new LinkedHashMap<>();

        protected void putWrapped(String key, Object value) {
            checkNotNull(key, "require key");
            if(value instanceof Number) {
                putNoWrap(key, value);
            } else {
                params.put(key, new ToStringWrapper(value));
            }
        }

        protected void putNoWrap(String key, Object value) {
            checkNotNull(key, "require key");
            params.put(key,value);
        }

        protected Map<String, Object> getParameters() {
            return params;
        }
    }


    protected static class LogFormatter {
        private static final String OUTCOME_IS_SUCCESS = "success";
        private static final String OUTCOME_IS_FAILURE = "failure";

        private final Logger logger;

        protected LogFormatter(Object actorOrLogger) {
            checkNotNull("require actor or logger");
            if(actorOrLogger instanceof Logger) {
                logger = (Logger) actorOrLogger;
            } else {
                logger = LoggerFactory.getLogger(actorOrLogger.getClass());
            }
        }

        public void log(final Operation operation) {
            final Map<String, Object> all = new LinkedHashMap<>();
            all.put("operation", operation.getName());
            all.putAll(operation.getParameters());

            logger.info(buildFormatString(all), buildArgumentArray(all));
        }

        protected void log(final Operation operation, Yield yield) {
            operation.terminated();

            final Map<String, Object> all = new LinkedHashMap<>();
            all.put("operation", operation.getName());
            all.put("outcome", OUTCOME_IS_SUCCESS);
            all.putAll(operation.getParameters());
            all.putAll(yield.getParameters());

            logger.info(buildFormatString(all), buildArgumentArray(all));
        }

        protected void log(final Operation operation, Failure failure) {
            operation.terminated();

            final Map<String, Object> all = new LinkedHashMap<>();
            all.put("operation", operation.getName());
            all.put("outcome", OUTCOME_IS_FAILURE);
            all.putAll(operation.getParameters());
            all.putAll(failure.getParameters());

            if(failure.didThrow()) {
                all.put("exception",new ToStringWrapper(failure.getThrown().toString()));

                logger.error(flatten(all), failure.getThrown());

            } else {
                logger.error(flatten(all));
            }
        }

        private String buildFormatString(final Map<String, Object> formatParameters) {
            final StringBuilder format = new StringBuilder();
            int i = formatParameters.size();
            for(String key : formatParameters.keySet()) {
                format.append(key).append("={}");
                i--;
                if(i > 0) format.append(" ");
            }
            return format.toString();
        }

        private Object[] buildArgumentArray(final Map<String, Object> formatParameters) {
            return formatParameters.values().toArray(new Object[formatParameters.size()]);
        }

        private String flatten(final Map<String, Object> formatParameters) {
            final StringBuilder flattened = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, Object> entry : formatParameters.entrySet()) {
                if(i > 0) flattened.append(" ");
                flattened
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
                i++;
            }
            return flattened.toString();
        }
    }

}
