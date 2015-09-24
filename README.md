Common Monitoring
=================

Operation
---------

Fluent splunk-friendly logging with automatic escaping; e.g


    import com.ft.membership.monitoring.Operation;

    import java.util.UUID;

    import static com.ft.membership.monitoring.Operation.operation;

    public class Demo {

        public static void main(String[] args) {
            new Demo().run();
        }

        protected void run() {
            // report starting conditions
            final Operation operation = operation("demo").with("id", UUID.randomUUID()).started(this);

            try {
                // do some things

                if(something) {
                    // report success
                    operation.wasSuccessful().yielding("result","{\"text\": \"hello world\"}").log();
                } else {
                    // report failure
                    operation.wasFailure().withMessage("no things").log();
                }


            } catch(Exception e) {
                // report failure
                operation.wasFailure().throwingException(e).log();
            }
        }
    }

might log:

    09:42:28.503 [main] INFO  Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0"
    09:42:28.543 [main] INFO  Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0" outcome=success result="{\"text\": \"hello world\"}"


on success, or:

    10:02:13.484 [main] ERROR Demo - operation=demo id="bd09f108-2a4d-4e47-8b9f-d20c19c0dad0" outcome=failure exception="java.lang.ArithmeticException: / by zero"
    java.lang.ArithmeticException: / by zero
        at Demo.run(Demo.java:19) [test-classes/:na]
        at Demo.main(Demo.java:10) [test-classes/:na]
        at ...

on failure.

The argument passed to the initiating ```started()``` (and, optionally, terminating ```log()```) call is used to derive the
logger name, and is usually the object which is the orchestrator of an operation. Alternatively, a specific
slf4j logger instance can be passed.

See https://sites.google.com/a/ft.com/technology/systems/membership/logging-conventions for suggested key names.

Maven Artifact
--------------
* http://anthill.svc.ft.com:8081/nexus/index.html#nexus-search;quick~common-monitoring

<pre>
   &lt;dependency>
       &lt;groupId>com.ft&lt;/groupId>
       &lt;artifactId>common-monitoring&lt;/artifactId>
       &lt;version>1.4&lt;/version>
   &lt;/dependency>
</pre>