package eu.reactivesystems.pantarhei.example5;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.reactivex.Flowable;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Replace the file source with a file source
 * from RxJava
 */
public class Example5 {

    public static void main(String args[]) {
        final ActorSystem system = ActorSystem.create("PanthaRei");
        final Materializer mat = ActorMaterializer.create(system);

        final Path file = Paths.get("macbeth.txt");

        Sink<String, CompletionStage<Done>> printlnSink =
                Sink.<String>foreach(System.out::println);


        /*
        Different source, from RxJava! Library Inter-Op!
         */
        Flowable<String> rxFile = Flowable.using(
                () -> new BufferedReader(new FileReader("macbeth.txt")),
                reader -> Flowable.fromIterable(() -> reader.lines().iterator()),
                reader -> reader.close()
        );


        final Source<String, NotUsed> linesSource = Source.fromPublisher(rxFile);

        final Source<Integer, NotUsed> numberSource = Source.range(1, 1000000)
                .throttle(1,
                        Duration.create(1, TimeUnit.SECONDS),
                        1,
                        ThrottleMode.shaping());

        numberSource
                .buffer(150, OverflowStrategy.backpressure())
                .zipWith(linesSource, (lineNum, line) -> lineNum + ": " + line)
                .runWith(printlnSink, mat)
                .whenComplete((result, ex) -> system.terminate());
    }
}
