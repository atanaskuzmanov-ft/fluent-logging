package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.event.Level;

public final class CompoundOperation implements AutoCloseable {

  private String name;
  private Parameters parameters;

  private boolean action;

  private Object actorOrLogger;
  static private OperationIdentity operationIdentity;
  static final Map<String, String> operationIdentityMap = new HashMap<>();

  public static void setOperationIdentity(OperationIdentity op) {
    operationIdentity = op;
  }

  CompoundOperation(
      final String name,
      final Object actorOrLogger,
      final Map<String, Object> parameters,
      final boolean action
  ) {
    checkNotNull(name, "provide a name for the name");
    checkNotNull(operationIdentity, "provide a function to resolve operation Identity");

    this.name = name;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
    this.action = action;

    if (action) {
      with(Key.Operation, operationIdentityMap.get(operationIdentity.getIdentity()));
    } else {
      operationIdentityMap.put(operationIdentity.getIdentity(), name);
    }
  }

  @FunctionalInterface
  public interface OperationIdentity {
    String getIdentity();
  }

  public static CompoundOperation operation(final String name, final Object actorOrLogger) {
    return new CompoundOperation(name, actorOrLogger, null, false);
  }

  public static CompoundOperation action(final String name, final Object actorOrLogger) {
    return new CompoundOperation(name, actorOrLogger, null, true);
  }

  public boolean isAction() {
    return action;
  }

  public CompoundOperation with(final Key key, final Object value) {
    return with(key.getKey(), value);
  }

  public CompoundOperation with(final String key, final Object value) {
    parameters.put(key, value);
    return this;
  }

  public CompoundOperation with(final Map<String, Object> keyValues) {
    parameters.putAll(keyValues);
    return this;
  }

  public CompoundOperation started() {
    new LogFormatter(actorOrLogger).logStart(this);
    return this;
  }

  public void wasSuccessful() {
    new LogFormatter(actorOrLogger).log(this, Outcome.Success, Level.INFO);
  }

  public void wasSuccessful(final Object result) {
    wasSuccessful(result, Level.INFO);
  }

  public void wasSuccessful(final Object result, final Level level) {
    with(Key.Result, result);
    wasSuccessful();
  }

  public void logDebug(final String debugMessage) {
    CompoundOperation compoundOperation = new CompoundOperation(
        name,
        actorOrLogger,
        parameters.getParameters(),
        isAction()
    );

    compoundOperation.with(Key.DebugMessage, debugMessage);
    new LogFormatter(actorOrLogger).log(compoundOperation, null, Level.DEBUG);
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

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
  }

  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
  }
}
