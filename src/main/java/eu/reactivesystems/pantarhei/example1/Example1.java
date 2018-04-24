package eu.reactivesystems.pantarhei.example1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * A simple example showing pushing data across an async boundary
 * without back pressure. The limited capacity queue represents
 * the fact that memory is finite.
 */
public class Example1 {

    public static void main(String args[]) {
        BlockingQueue q = new ArrayBlockingQueue(150);
        Producer p = new Producer(q);
        Consumer c = new Consumer(q);
        new Thread(p).start();
        new Thread(c).start();
    }
}

class Producer implements Runnable {
    private final BlockingQueue queue;

    Producer(final BlockingQueue q) {
        queue = q;
    }

    public void run() {
        try (Stream<String> stream = Files.lines(Paths.get("macbeth.txt"))) {
            stream.forEach(queue::add);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class Consumer implements Runnable {
    private final BlockingQueue queue;

    Consumer(BlockingQueue q) {
        queue = q;
    }

    public void run() {
        try {
            while (true) {
                // Thread.sleep(200);
                System.out.println(queue.take());
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
