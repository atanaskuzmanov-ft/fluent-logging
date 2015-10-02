package com.ft.membership.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.ft.membership.logging.LogFormatter.NameAndValue.nameAndValue;
import static com.google.common.base.Preconditions.checkNotNull;

class LogFormatter {
    private static final String OUTCOME_IS_SUCCESS = "success";
    private static final String OUTCOME_IS_FAILURE = "failure";

    private final Logger logger;

    protected LogFormatter(Object actorOrLogger) {
        checkNotNull("require actor or logger");
        if (actorOrLogger instanceof Logger) {
            logger = (Logger) actorOrLogger;
        } else {
            logger = LoggerFactory.getLogger(actorOrLogger.getClass());
        }
    }

    public void logInfo(final Operation operation) {
        final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
        addOperation(operation, msgParams);
        addOperationParameters(operation, msgParams);
        if (logger.isInfoEnabled()) {
            logger.info(buildMsgString(msgParams));
        }
    }

    protected void logInfo(final Operation operation, Yield yield) {
        operation.terminated();

        final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
        addOperation(operation, msgParams);
        addOutcome(OUTCOME_IS_SUCCESS, msgParams);
        addOperationParameters(operation, msgParams);
        addYield(yield, msgParams);

        if (logger.isInfoEnabled()) {
            logger.info(buildMsgString(msgParams));
        }
    }

    protected void logError(final Operation operation, Yield yield) {
        operation.terminated();

        final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
        addOperation(operation, msgParams);
        addOutcome(OUTCOME_IS_SUCCESS, msgParams);
        addOperationParameters(operation, msgParams);
        addYield(yield, msgParams);

        if (logger.isErrorEnabled()) {
            logger.error(buildMsgString(msgParams));
        }
    }

    protected void logInfo(Operation operation, Failure failure) {
        operation.terminated();
        
        if (logger.isInfoEnabled()) {
            String failureMessage = buildFailureMessage(operation, failure);
            logger.info(failureMessage);
        }
    }

    protected void logError(final Operation operation, Failure failure) {
        operation.terminated();
        
        if (logger.isErrorEnabled()) {
            String failureMessage = buildFailureMessage(operation, failure);

            if (failure.didThrow()) {
                logger.error(failureMessage, failure.getThrown());
            } else {
                logger.error(failureMessage);
            }
        }
    }

    private String buildFailureMessage(final Operation operation, Failure failure) {
        final Collection<NameAndValue> msgParams = new ArrayList<NameAndValue>();
        addOperation(operation, msgParams);
        addOutcome(OUTCOME_IS_FAILURE, msgParams);
        addFailureMessage(failure, msgParams);
        addOperationParameters(operation, msgParams);
        addFailureDetails(failure, msgParams);
        return buildMsgString(msgParams);
    }
    
    private String buildMsgString(final Collection<NameAndValue> msgParams) {
        final StringBuilder sb = new StringBuilder();
        boolean addSeperator = false;
        for (NameAndValue msgParam : msgParams) {
            if (addSeperator) {
                sb.append(" ");
            }
            sb.append(msgParam);
            addSeperator = true;
        }
        return sb.toString();
    }
    
    private void addOperation(final Operation operation, final Collection<NameAndValue> msgParams) {
        msgParams.add(nameAndValue("operation", operation.getName()));
    }

    private void addOperationParameters(final Operation operation, final Collection<NameAndValue> msgParams) {
        addParametersAsNamedValues(msgParams, operation.getParameters());
    }

    private void addOutcome(String outcome, final Collection<NameAndValue> msgParams) {
        msgParams.add(nameAndValue("outcome", outcome));
    }

    private void addYield(Yield yield, final Collection<NameAndValue> msgParams) {
        addParametersAsNamedValues(msgParams, yield.getParameters());
    }

    private void addParametersAsNamedValues(final Collection<NameAndValue> msgParams, Map<String, Object> parameters) {
        for (String name : parameters.keySet()) {
            msgParams.add(nameAndValue(name, parameters.get(name)));
        }
    }

    private void addFailureMessage(Failure failure, final Collection<NameAndValue> msgParams) {
        msgParams.add(nameAndValue("errorMessage", failure.getFailureMessage()));
    }

    private void addFailureDetails(Failure failure, final Collection<NameAndValue> msgParams) {
        addParametersAsNamedValues(msgParams, failure.getParameters());

        if (failure.didThrow()) {
            msgParams.add(nameAndValue("exception", failure.getThrown().toString()));
        }
    }

    static class NameAndValue {
        
        private String name;
        private Object value;

        static NameAndValue nameAndValue(String name, Object value) {
            return new NameAndValue(name, value);
        }
        
        private NameAndValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }
        
        public String toString() {
            if (value instanceof Number) {
                return String.format("%s=%d", name, value);
            } else {
                return String.format("%s=%s", name, new ToStringWrapper(value).toString());
            }
        }
        
    }
    
}
