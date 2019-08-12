package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class SuccessState implements OperationState {
  private final OperationContext context;
  private final String type;

  SuccessState(OperationContext operationContext) {
    context = operationContext;
    type = context.getType();
    context.setState(this);

    context.log(Outcome.Success, Level.INFO);
  }

  @Override
  public void with(Map<String, Object> keyValues) {}

  @Override
  public void with(String key, Object value) {}

  @Override
  public String getType() {return type;}

  @Override
  public void start() {}

  @Override
  public void succeed() {}

  @Override
  public void fail() {}
}
