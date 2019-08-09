package com.ft.membership.t;

import static com.ft.membership.logging.CompoundOperation.operation;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.ft.membership.logging.CompoundOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CompoundOperationTest {

  @Mock
  Logger mockLogger;

  @Before
  public void setup() {
    Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
    Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
  }

  @Test
  public void log_start_and_success() throws Exception {

    CompoundOperation.operation("compound_success", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("operation=\"compound_success\"");
    verify(mockLogger).info("operation=\"compound_success\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }


  @Test
  public void compound_operation_should_have_fluent_api() throws Exception {
    CompoundOperation operation = operation("getUserSubscriptions", mockLogger)
        .with("userId", "1234")
        .started();

    operation.logDebug("The user has a lot of subscriptions");
    operation.with("activeSubscription", "S-12345");
    operation.wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger, times(1)).isDebugEnabled();
    verify(mockLogger).info("operation=\"getUserSubscriptions\" userId=\"1234\"");
    verify(mockLogger).debug(
        "operation=\"getUserSubscriptions\" userId=\"1234\" debugMessage=\"The user has a lot of subscriptions\"");
    verify(mockLogger).info(
        "operation=\"getUserSubscriptions\" outcome=\"success\" userId=\"1234\" activeSubscription=\"S-12345\"");
    verifyNoMoreInteractions(mockLogger);
  }

  @Test
  public void log_simple_action() throws Exception {
    CompoundOperation.action("compound_action", mockLogger).started().wasSuccessful();

    verify(mockLogger, times(2)).isInfoEnabled();
    verify(mockLogger).info("action=\"compound_action\"");
    verify(mockLogger).info("action=\"compound_action\" outcome=\"success\"");
    verifyNoMoreInteractions(mockLogger);
  }

}
