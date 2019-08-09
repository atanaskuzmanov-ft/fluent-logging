package com.ft.membership.t;

import com.ft.membership.logging.CompoundOperation;
import com.ft.membership.logging.Key;
import com.ft.membership.logging.Operation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ft.membership.logging.Operation.operation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

        verify(mockLogger,times(2)).isInfoEnabled();
        verify(mockLogger).info("operation=\"compound_success\"");
        verify(mockLogger).info("operation=\"compound_success\" outcome=\"success\"");
        verifyNoMoreInteractions(mockLogger);
    }

}
