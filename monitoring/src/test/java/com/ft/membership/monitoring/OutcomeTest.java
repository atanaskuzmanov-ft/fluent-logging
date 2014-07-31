package com.ft.membership.monitoring;

import com.ft.membership.domain.UserId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static com.ft.membership.monitoring.Outcome.DomainObjectKey.ErightsGroupId;
import static com.ft.membership.monitoring.Outcome.DomainObjectKey.ErightsId;
import static com.ft.membership.monitoring.Outcome.operation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OutcomeTest {

    @Mock
    Logger logger;

    @Test
    public void should_log_simple_success() throws Exception {

        operation("simple_success").wasSuccessful().log(logger);

        verify(logger).info(
                "operation={} outcome={}",
                new Object[]{"simple_success", "success"});

    }

    @Test
    public void should_log_success_for_user_id() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success").with(userId).wasSuccessful().log(logger);

        verify(logger).info(
                eq("operation={} outcome={} userId={}"),
                eq("simple_success"),
                eq("success"),
                eq(userId)
        );
    }

    @Test
    public void should_log_success_for_user_id_and_other_params() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(userId)
                .with("x", 100)
                .with("y", "that quick brown fox")
                .wasSuccessful()
                .log(logger);

        verify(logger).info(
                eq("operation={} outcome={} userId={} x={} y={}"),
                eq("simple_success"),
                eq("success"),
                eq(userId),
                eq(100),
                eq(new ToStringWrapper("that quick brown fox"))
        );
    }


    @Test
    public void should_log_success_for_user_id_and_key_params() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(userId)
                .with(ErightsId,1000)
                .with("y", "that quick brown fox")
                .wasSuccessful()
                .log(logger);

        verify(logger).info(
                eq("operation={} outcome={} userId={} erightsId={} y={}"),
                eq("simple_success"),
                eq("success"),
                eq(userId),
                eq(1000),
                eq(new ToStringWrapper("that quick brown fox"))
        );
    }

    @Test
    public void should_log_success_for_user_id_and_key_params_yielding() throws Exception {

        final UserId userId = UserId.randomUserId();
        operation("simple_success")
                .with(ErightsId,1000)
                .with("y", "that quick brown fox")
                .wasSuccessful()
                .yielding(userId)
                .yielding(ErightsGroupId, 100)
                .log(logger);

        verify(logger).info(
                eq("operation={} outcome={} erightsId={} y={} userId={} erightsGroupId={}"),
                eq("simple_success"),
                eq("success"),
                eq(1000),
                eq(new ToStringWrapper("that quick brown fox")),
                eq(userId),
                eq(100)
        );
    }


    @Test
    public void should_log_simple_failure() throws Exception {

        operation("simple_failure").wasFailure().withMessage("boo hoo").log(logger);

        verify(logger).error(
                eq("operation=simple_failure outcome=failure errorMessage=\"boo hoo\"")
                );
    }

    @Test
    public void should_log_failure_with_exception() throws Exception {


        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure").wasFailure().throwingException(ex).log(logger);

        verify(logger).error(
                eq("operation=simple_failure outcome=failure exception=\"java.lang.RuntimeException: bang!\""),
                eq(ex)
        );
    }

    @Test
    public void should_log_failure_with_parameters_and_exception() throws Exception {


        final Exception ex = new RuntimeException("bang!");
        operation("simple_failure")
                .with("x",101)
                .with("y","bat")
                .wasFailure()
                .throwingException(ex)
                .withMessage("got a puncture")
                .withDetail("tyre","right")
                .log(logger);

        verify(logger).error(
                eq("operation=simple_failure outcome=failure x=101 y=\"bat\" errorMessage=\"got a puncture\" tyre=\"right\" exception=\"java.lang.RuntimeException: bang!\""),
                eq(ex)
        );
    }

    @Test
    public void should_allow_null_values_for_parameters_and_yields() throws Exception {
        operation("allow_nulls")
                .with("nullableInput",null)
                .wasSuccessful()
                .yielding("nullableResult",null)
                .log(logger);

        verify(logger).info(
                eq("operation={} outcome={} nullableInput={} nullableResult={}"),
                eq("allow_nulls"),
                eq("success"),
                eq(new ToStringWrapper(null)),
                eq(new ToStringWrapper(null))
        );

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_values_for_operation_keys() throws Exception {
        operation("no_null_operation_keys")
                .with((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_values_for_yield_keys() throws Exception {
        operation("no_null_yield_keys")
                .wasSuccessful().yielding((String) null, "aValue");

    }

    @Test(expected = NullPointerException.class)
    public void should_not_allow_null_value_for_exception() throws Exception {
        operation("no_null_yield_keys")
                .wasFailure().throwingException(null).log(logger);

    }

    @Test
    public void should_log_error_if_used_in_try_with_resources_and_not_terminated() throws Exception {

        try(Outcome.Operation operation = operation("try-with-resources").with("a", 5).started(logger)) {
            // do nothing
        }

        verify(logger).error(
                eq("operation=try-with-resources outcome=failure a=5 exception=\"java.lang.RuntimeException: operation auto-closed\""),
                any(RuntimeException.class)
        );
    }

    @Test
    public void should_log_normally_if_used_in_try_with_resources_and_properly_terminated() throws Exception {

        try(Outcome.Operation operation = operation("try-with-resources").with("a", 5).started(logger)) {
            operation.wasSuccessful().log(logger);
        }

        verify(logger).info(
                eq("operation={} outcome={} a={}"),
                eq("try-with-resources"),
                eq("success"),
                eq(5)
        );
    }

}
