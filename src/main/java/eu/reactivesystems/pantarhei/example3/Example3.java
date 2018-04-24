package eu.reactivesystems.pantarhei.example3;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import scala.concurrent.duration.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Some additional tranformations, and some backpressure..
 */
public class Example3 {

    public static void main(String args[]) {
        final ActorSystem system = ActorSystem.create("PanthaRei");
        final Materializer mat = ActorMaterializer.create(system);

        final Path file = Paths.get("macbeth.txt");

        Sink<String, CompletionStage<Done>> printlnSink =
                Sink.<String>foreach(System.out::println);


        final Source<String, CompletionStage<IOResult>> linesSource = FileIO.fromPath(file)
                .via(Framing.delimiter(ByteString.fromString("\n"), 255))
                .map(ByteString::utf8String);

        /* Additional source, throttled down. Note how it will slow
        down the whole process.
         */
        final Source<Integer, NotUsed> numberSource = Source.range(1, 1000000);
/*                .throttle(1,
                        Duration.create(1, TimeUnit.SECONDS),
                        1,
                        ThrottleMode.shaping());
*/
        numberSource
                .buffer(150, OverflowStrategy.backpressure())
                .zipWith(linesSource, (lineNum, line) -> lineNum + ": " + line)
                .runWith(printlnSink, mat).whenComplete((result, ex) -> system.terminate());
    }
}
