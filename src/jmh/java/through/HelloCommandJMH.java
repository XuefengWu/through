package through;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class HelloCommandJMH {

    @State(Scope.Thread)
    public static class CommandState {
        Command<String> command;
        @Setup(Level.Invocation)
        public void setUp() {
            command = new Command<String>() {
                @Override
                public Result<String> run() {
                    /*try {
                        Thread.sleep(new Random().nextInt() * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    return new Result.Ok("Hello");
                }
            };
        }
    }

    @TearDown
    public void tearDown() throws InterruptedException {
        System.out.println("Down");

        CommandService.shutdown();
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public String helloCommandExecution(CommandState state) throws ExecutionException, InterruptedException {
        return CommandService.submit(state.command).get().value().get();
        //return new Result.Ok("Hello: ").value().get().toString();
    }



    public static void main(String[] args) throws RunnerException {
        /*System.out.println("Ready");
        Scanner scanIn = new Scanner(System.in);
        scanIn.nextLine();
        scanIn.close();
        System.out.println("Go");*/
        Options opt = new OptionsBuilder()
                .include(HelloCommandJMH.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();

    }

}
