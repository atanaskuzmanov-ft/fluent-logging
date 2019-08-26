package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.event.Level;

public final class SimpleOperationContext extends OperationContext {
  static private OperationIdentity operationIdentity;
  static final Map<String, String> operationIdentityMap = new HashMap<>();

  public static void setOperationIdentity(OperationIdentity op) {
    operationIdentity = op;
  }

  SimpleOperationContext(
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

  public static SimpleOperationContext operation(final String name, final Object actorOrLogger) {
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    new OperationConstructedState(context);
    return context;
  }

  public static SimpleOperationContext action(final String name, final Object actorOrLogger) {
    final SimpleOperationContext context = new SimpleOperationContext(name, actorOrLogger, null);
    new ActionConstructedState(context);
    return context;
  }

  public void wasSuccessful() {
    state.succeed();
    cleanupIdentity();
  }

  public void wasSuccessful(final Object result) {
    with(Key.Result, result);
    this.state.succeed();
    cleanupIdentity();
  }

  public void wasFailure() {
    state.fail();
    cleanupIdentity();
  }

  public void wasFailure(final Object result) {
    with(Key.Result, result);
    state.fail();
    cleanupIdentity();
  }

  public void logDebug(final String debugMessage, final Map<String, Object> keyValues) {
    SimpleOperationContext debugSimpleOperationContext = new SimpleOperationContext(
        name,
        actorOrLogger,
        parameters.getParameters()
    );

    IsolatedState.from(debugSimpleOperationContext, this.state.getType());

    debugSimpleOperationContext.with(Key.DebugMessage, debugMessage);
    debugSimpleOperationContext.with(keyValues);

    new LogFormatter(actorOrLogger).log(debugSimpleOperationContext, null, Level.DEBUG);
  }

  @Override
  public void close() {
    if (!(state instanceof FailState || state instanceof StartedState)) {
      wasFailure("\"Programmer error: operation auto-closed before wasSuccessful() or wasFailure() called.\"");
    }

    cleanupIdentity();
  }

  void cleanupIdentity() {
    if (state.getType() == "operation") {
      final String id = operationIdentity.getIdentity();
      operationIdentityMap.remove(id);
    }
  }

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

}
