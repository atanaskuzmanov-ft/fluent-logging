package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class OperationConstructedState implements OperationState {
  private OperationContext context;
  private final String type = "operation";

  OperationConstructedState(final OperationContext operationContext) {
    context = operationContext;
    context.setState(this);

    context.addIdentity(context.getName());
  }

  @Override
  public void with(Map<String, Object> keyValues) {
    context.addParam(keyValues);
  }

  @Override
  public void with(String key, Object value) {
    context.addParam(key, value);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void start() {
    context.setState(new StartedState(context));
  }

  @Override
  public void succeed() {
    context.setState(new SuccessState(context));
  }

  @Override
  public void fail() {
    context.setState(new FailState(context));
  }
}
