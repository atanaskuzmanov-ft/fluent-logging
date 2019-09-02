package com.ft.membership.logging;

import static com.ft.membership.logging.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.event.Level;

public final class SimpleOperationContext extends OperationContext {
  SimpleOperationContext(
      final String name,
      final Object actorOrLogger,
      final Map<String, Object> parameters
  ) {
    checkNotNull(name, "provide a name for the name");

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

  @Override
  protected void clear() {
    MDC.remove(state.getType());
  }

  // Needed for linking operations with actions
  void addIdentity(final String name) {
    MDC.put(state.getType(), name);
  }

}
