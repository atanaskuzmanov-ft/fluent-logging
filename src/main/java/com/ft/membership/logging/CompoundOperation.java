package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;
import java.util.Optional;

import static com.ft.membership.logging.Preconditions.checkNotNull;

/**
 * An Operation is a logging context with starting parameters, which either succeeds or fails, supporting additional
 * detail as to what happened. It logs in a <tt>key="value"</tt> format suitable for parsing by a log aggregator such as
 * Splunk, escaping quotes with back-slashes.
 *
 * <p>e.g.</p>
 *
 * <pre>
 *     Operation operation = Operation.operation("launch")
 *         .with("probe", probe)
 *         .with("target", target)
 *         .started(this);
 *
 *     try {
 *         Flight f = probe.launch();
 *
 *         operation.wasSuccessful()
 *             .yielding("position", f.position())
 *             .log();
 *
 *     } catch(LaunchException e) {
 *         operation.wasFailure()
 *             .throwingException(e)
 *             .withMessage(e.reason())
 *             .withDetail("malfunction", e.malfunction())
 *             .log();
 *     }
 * </pre>
 * On success, this would log something like:
 * <pre>
 *     INFO operation="launch" probe=27 target="Mars"
 *     ...
 *     INFO operation="launch" probe=27 target="Mars" position="[10.0,17.2,0.0]" outcome="success"
 * </pre>
 *
 * or on failure:
 * <pre>
 *     INFO operation="launch" probe=27 target="Mars"
 *     ...
 *     ERROR operation="launch" probe=27 target="Mars" outcome="failure" message="crash" malfunction="parachute" exception="LaunchException"
 *     LaunchException: exception message
 *     ...stacktrace...
 * </pre>
 *
 */
public class CompoundOperation implements AutoCloseable {

    private String operation;
    private Object actorOrLogger;

    private Parameters parameters;

    CompoundOperation(final String operation) {
        checkNotNull(operation, "require operationName");
        this.operation = operation;

    }

    /**
     * create an Operation, ready for decoration with parameters.
     * @param operation name of the operation.
     * @return an Operation.
     */
    public static CompoundOperation operation(final String operation, final Object actorOrLogger) {
        return new CompoundOperation(operation, actorOrLogger, null);
    }

    public static CompoundOperation action(final String action) {
        return new CompoundOperation(action);
    }


    public CompoundOperation(final String operationName, final Object actorOrLogger, final Map<String, Object> parameters) {
        this.operation = operationName;
        this.actorOrLogger = actorOrLogger;
        this.parameters = Parameters.parameters(parameters);
    }

    /**
     * add a starting parameter.
     * @param key a log key
     * @param value a value
     * @return Operation
     */
    public CompoundOperation with(final Key key, final Object value) {
        return with(key.getKey(), value);
    }

    /**
     * add a starting parameter.
     * @param key a log key string
     * @param value a value
     * @return Operation
     */
    public CompoundOperation with(final String key, final Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * add starting parameters from entries in a map.
     * @param keyValues a map of parameter key-values
     * @return Operation
     */
    public CompoundOperation with(final Map<String, Object> keyValues) {
        parameters.putAll(keyValues);
        return this;
    }


    public CompoundOperation started() {
        new LogFormatter(actorOrLogger).logStart(this);
        return this;
    }

    /**
     * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting the operation.
     */
    public void wasSuccessful() {
        log(getActorOrLogger());
    }

    public void wasSuccessful(final Object result) {
        with(Key.Result, result);
        log(getActorOrLogger());
    }

    String getName() {
        return operation;
    }

    Map<String, Object> getParameters() {
        return parameters.getParameters();
    }

    Object getActorOrLogger() {
        return actorOrLogger;
    }


    /**
     * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting the operation.
     */
    public void log() {
        log(getActorOrLogger());
    }

    /**
     * log this success with an <tt>INFO</tt> log-level
     * @param actorOrLogger an alternative logger or object for log context
     */
    public void log(final Object actorOrLogger) {
        logInfo(actorOrLogger);
    }

    /**
     * log this success with a <tt>DEBUG</tt> log-level, instead of <tt>INFO</tt>.
     * @param actorOrLogger logger or object for log context
     */
    public void logDebug(Object actorOrLogger) {
        new LogFormatter(actorOrLogger).log(this, Outcome.Success, Level.DEBUG);
    }

    private void logInfo(Object actorOrLogger) {
        new LogFormatter(actorOrLogger).log(this, Outcome.Success, Level.INFO);
    }

    private void logError(Object actorOrLogger) {
        new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
    }


    @Override
    public void close() {

    }

}
