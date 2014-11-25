package com.ft.membership.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

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
        final Map<String, Object> all = new LinkedHashMap<>();
        addOperation(operation, all);

        logger.info(buildFormatString(all), buildArgumentArray(all));
    }

    protected void logInfo(final Operation operation, Yield yield) {
        operation.terminated();

        final Map<String, Object> all = new LinkedHashMap<>();
        addOperation(operation, all);
        addOutcome(OUTCOME_IS_SUCCESS, all);
        addYield(yield, all);

        logger.info(buildFormatString(all), buildArgumentArray(all));
    }

    protected void logError(final Operation operation, Yield yield) {
        operation.terminated();

        final Map<String, Object> all = new LinkedHashMap<>();
        addOperation(operation, all);
        addOutcome(OUTCOME_IS_SUCCESS, all);
        addYield(yield, all);

        logger.error(buildFormatString(all), buildArgumentArray(all));
    }

    protected void logInfo(Operation operation, Failure failure) {
        operation.terminated();
        if (logger.isInfoEnabled()) {
            String failureMessage = buildFailureMessage(operation, failure);

            if (failure.didThrow()) {
                logger.info(failureMessage, failure.getThrown());
            } else {
                logger.info(failureMessage);
            }
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

    private String buildFormatString(final Map<String, Object> formatParameters) {
        final StringBuilder format = new StringBuilder();
        int i = formatParameters.size();
        for (String key : formatParameters.keySet()) {
            format.append(key).append("={}");
            i--;
            if (i > 0) format.append(" ");
        }
        return format.toString();
    }

    private Object[] buildArgumentArray(final Map<String, Object> formatParameters) {
        return formatParameters.values().toArray(new Object[formatParameters.size()]);
    }

    private String buildFailureMessage(final Operation operation, Failure failure) {
        final Map<String, Object> all = new LinkedHashMap<>();
        addOperation(operation, all);
        addOutcome(OUTCOME_IS_FAILURE, all);
        addFailure(failure, all);
        return flatten(all);
    }

    private void addOperation(final Operation operation, final Map<String, Object> msgParams) {
        msgParams.put("operation", operation.getName());
        msgParams.putAll(operation.getParameters());
    }

    private void addOutcome(String outcome, final Map<String, Object> msgParams) {
        msgParams.put("outcome", outcome);
    }

    private void addYield(Yield yield, final Map<String, Object> all) {
        all.putAll(yield.getParameters());
    }

    private void addFailure(Failure failure, final Map<String, Object> msgParams) {
        msgParams.putAll(failure.getParameters());

        if (failure.didThrow()) {
            msgParams.put("exception", new ToStringWrapper(failure.getThrown().toString()));
        }
    }

    private String flatten(final Map<String, Object> msgParameters) {
        final StringBuilder flattened = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : msgParameters.entrySet()) {
            if (i > 0) flattened.append(" ");
            flattened
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            i++;
        }
        return flattened.toString();
    }
}
