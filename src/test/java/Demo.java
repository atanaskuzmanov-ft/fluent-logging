import com.ft.membership.monitoring.Operation;

import java.util.UUID;

import static com.ft.membership.monitoring.Operation.operation;

public class Demo {

    public static void main(String[] args) {
        new Demo().run();
    }

    protected void run() {
        // report starting conditions
        final Operation operation = operation("demo").with("argument", UUID.randomUUID()).started(this);

        try {
            // do some things
            int x = 1/0;
            // report success
            operation.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();

        } catch(Exception e) {
            // report failure
            operation.wasFailure().throwingException(e).log();
        }
    }
}
