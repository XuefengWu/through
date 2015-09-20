package through;

import java.util.concurrent.ExecutionException;


public class CommandDemo extends Command<String> {
    private int num;

    public CommandDemo(int num) {
        this.num = num;
    }

    @Override
    public Result run() {
        String message = "Demo Demo: " + num;
        return new Result.Ok(message);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        for (int i = 0; i < 100; i++) {
            CommandService.submit(new CommandDemo(i));
        }

    }

}
