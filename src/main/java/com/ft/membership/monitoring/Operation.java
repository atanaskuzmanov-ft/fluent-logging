package com.ft.membership.monitoring;

import com.ft.membership.common.types.userid.UserId;
import com.google.common.base.Optional;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Operation implements AutoCloseable {

    private final String operationName;
    private Object actorOrLogger;

    private boolean terminated;

    private Map<String, Object> parameters;

    public static OperationBuilder operation(final String operation) {
        return new OperationBuilder(operation);
    }

    public Operation(final String operationName, final Object actorOrLogger, final Map<String, Object> parameters) {
        this.operationName = operationName;
        this.actorOrLogger = actorOrLogger;
        this.parameters = parameters;
    }

    public static class OperationBuilder extends Parameters {

        private final String operationName;
        private Object actorOrLogger;

        public OperationBuilder( final String operationName){
            checkNotNull(operationName, "require operationName");
            this.operationName = operationName;
        }

        public OperationBuilder with(final UserId id) {
            putNoWrap(DomainObjectKey.UserId.getKey(), id);
            return this;
        }

        public OperationBuilder with(final DomainObjectKey key, final Object value) {
            return with(key.getKey(), value);
        }

        public OperationBuilder with(final String key, final Object value) {
            putWrapped(key, value);
            return this;
        }

        public Operation started(final Object actorOrLogger) {
            final Operation operation = new Operation(operationName, actorOrLogger, getParameters());
            new LogFormatter(actorOrLogger).logInfo(operation);
            return operation;
        }
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
        return operationName;
    }

    protected Map<String, Object> getParameters() {
        return parameters;
    }

    protected Object getActorOrLogger() {
        return actorOrLogger;
    }

    @Override
    public void close() {
        if (!terminated) {
            this.wasFailure()
                    .throwingException(new RuntimeException("operation auto-closed")) // so we at least get a stack-trace
                    .log(Optional.fromNullable(actorOrLogger).or(this));
        }
    }

}
