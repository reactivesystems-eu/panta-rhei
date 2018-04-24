package eu.reactivesystems.pantarhei.example2;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

/**
 * A simple example that does the same as
 * example 1 does, but this time it works,
 * for any file size.
 */
public class Example2 {

    public static void main(String args[]) {
        final ActorSystem system = ActorSystem.create("PanthaRei");
        final Materializer mat = ActorMaterializer.create(system);

        final Path file = Paths.get("macbeth.txt");

        Sink<String, CompletionStage<Done>> printlnSink =
                Sink.<String>foreach(System.out::println);


        FileIO.fromPath(file)
                .via(Framing.delimiter(ByteString.fromString("\n"), 255))
                .map(ByteString::utf8String)
                .buffer(150, OverflowStrategy.backpressure())
                .runWith(printlnSink, mat).whenComplete((result, ex) -> system.terminate());
    }
}
