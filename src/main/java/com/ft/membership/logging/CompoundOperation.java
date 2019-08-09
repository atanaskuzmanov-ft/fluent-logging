package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Map;
import org.slf4j.event.Level;

public class CompoundOperation implements AutoCloseable {

  private String name;
  private Object actorOrLogger;

  private Parameters parameters;
  private boolean action;

  CompoundOperation(
      final String name,
      final Object actorOrLogger,
      final Map<String, Object> parameters,
      final boolean action
  ) {
    checkNotNull(name, "provide a name for the name");

    this.name = name;
    this.actorOrLogger = actorOrLogger;
    this.parameters = Parameters.parameters(parameters);
    this.action = action;
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
    with(Key.Result, result);
    log(getActorOrLogger());
  }

  public void log() {
    log(getActorOrLogger());
  }

  public void log(final Object actorOrLogger) {
    logInfo(actorOrLogger);
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


  private void logInfo(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, null, Level.INFO);
  }

  private void logError(Object actorOrLogger) {
    new LogFormatter(actorOrLogger).log(this, Outcome.Failure, Level.ERROR);
  }


}
