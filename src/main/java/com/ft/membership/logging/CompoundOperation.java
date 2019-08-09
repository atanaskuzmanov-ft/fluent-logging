package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Map;
import org.slf4j.event.Level;

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
   *
   * @param operation name of the operation.
   * @return an Operation.
   */
  public static CompoundOperation operation(final String operation, final Object actorOrLogger) {
    return new CompoundOperation(operation, actorOrLogger, null);
  }

  public static CompoundOperation action(final String action) {
    return new CompoundOperation(action);
  }


  public CompoundOperation(final String operationName, final Object actorOrLogger,
      final Map<String, Object> parameters) {
    this.operation = operationName;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
  }

  /**
   * add a starting parameter.
   *
   * @param key a log key
   * @param value a value
   * @return Operation
   */
  public CompoundOperation with(final Key key, final Object value) {
    return with(key.getKey(), value);
  }

  /**
   * add a starting parameter.
   *
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
   *
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
   * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting
   * the operation.
   */
  public void wasSuccessful() {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
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
   * log this success with an <tt>INFO</tt> log-level, using the log context passed when starting
   * the operation.
   */
  public void log() {
    log(getActorOrLogger());
  }

  /**
   * log this success with an <tt>INFO</tt> log-level
   *
   * @param actorOrLogger an alternative logger or object for log context
   */
  public void log(final Object actorOrLogger) {
    logInfo(actorOrLogger);
  }

  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
  }

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
  }

  public void logDebug(final String debugMessage) {
    parameters.put(Key.DebugMessage, debugMessage);
    new LogFormatter(actorOrLogger).log(this, null, Level.DEBUG);
  }

  @Override
  public void close() {

  }

}
