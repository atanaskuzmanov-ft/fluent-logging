package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.event.Level;

public final class OperationContext implements AutoCloseable {

  private OperationState state;
  private String name;
  private Parameters parameters;
  private Object actorOrLogger;

  static private OperationIdentity operationIdentity;
  static final Map<String, String> operationIdentityMap = new HashMap<>();

  public static void setOperationIdentity(OperationIdentity op) {
    operationIdentity = op;
  }

  OperationContext(
      final String name,
      final Object actorOrLogger,
      final Map<String, Object> parameters
  ) {
    checkNotNull(name, "provide a name for the name");
    checkNotNull(operationIdentity, "provide a function to resolve operation Identity");

    this.name = name;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
  }

  public static OperationContext operation(final String name, final Object actorOrLogger) {
    final OperationContext context = new OperationContext(name, actorOrLogger, null);
    new OperationConstructedState(context);
    return context;
  }

  public static OperationContext action(final String name, final Object actorOrLogger) {
    final OperationContext context = new OperationContext(name, actorOrLogger, null);
    new ActionConstructedState(context);
    return context;
  }

  public OperationContext started() {
    state.start();
    return this;
  }

  public OperationContext with(final Key key, final Object value) {
    return with(key.getKey(), value);
  }

  public OperationContext with(final String key, final Object value) {
    state.with(key, value);
    return this;
  }

  public OperationContext with(final Map<String, Object> keyValues) {
    state.with(keyValues);
    return this;
  }

  public void wasSuccessful() {
    state.succeed();
  }

  public void wasSuccessful(final Object result) {
    with(Key.Result, result);
  }

  public void wasSuccessful(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  public void wasFailure() {
    state.fail();
  }

  public void wasFailure(final Object result) {
    with(Key.Result, result);
    state.fail();
  }

  public void wasFailure(final Object result, final Level level) {
    // TODO decide if we want to support different levels of result logs
  }

  void log(final Outcome outcome, final Level logLevel) {
    new LogFormatter(actorOrLogger).log(this, outcome, logLevel);
  }

  void addParam(final String key, final Object value) {
    parameters.put(key, value);
  }

  void addParam(final Map<String, Object> keyValues) {
    parameters.putAll(keyValues);
  }

  public void logDebug(final String debugMessage) {
    logDebug(debugMessage, Collections.emptyMap());
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    OperationContext operationContext = new OperationContext(
        name,
        actorOrLogger,
        parameters.getParameters()
    );

    new IsolatedState(operationContext, this.state.getType());

    operationContext.with(Key.DebugMessage, debugMessage);
    operationContext.with(keyValues);

    new LogFormatter(actorOrLogger).log(operationContext, null, Level.DEBUG);
  }

  @Override
  public void close() {
    final String id = operationIdentity.getIdentity();
    operationIdentityMap.remove(id);
  }

  String getName() {
    return name;
  }

  Map<String, Object> getParameters() {
    return parameters.getParameters();
  }

  Object getActorOrLogger() {
    return actorOrLogger;
  }

  String getType() {
    return this.state.getType();
  }

  void setState(OperationState operationState) {
    this.state = operationState;
  };

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    operationIdentityMap.put(operationIdentity.getIdentity(), name);
  }

  // Needed for linking operations with actions
  String getCurrentOperation() {
    return operationIdentityMap.get(operationIdentity.getIdentity());
  }

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
  }

  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
  }

  public void log(Level level) {
    log(null, level);
  }
}
