package com.ft.membership.logging;

import org.slf4j.event.Level;

import java.util.Map;

public class IsolatedState implements OperationState {
  private String type;
  private OperationContext context;

  IsolatedState(OperationContext operationContext, String type) {
    context = operationContext;
    this.type = type;
    context.setState(this);
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
  public void start() {}

  @Override
  public void succeed() {}

  @Override
  public void fail() {}
}
