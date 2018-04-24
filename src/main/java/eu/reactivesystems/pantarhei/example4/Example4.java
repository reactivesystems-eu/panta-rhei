package eu.reactivesystems.pantarhei.example4;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * Write to Cassandra
 */
public class Example4 {


    public static void main(String args[]) {
        final ActorSystem system = ActorSystem.create("PanthaRei");
        final Materializer mat = ActorMaterializer.create(system);

        final Session session = Cluster.builder()
                .addContactPoint("127.0.0.1").withPort(9042)
                .build().connect();

        final PreparedStatement preparedStatement =
                session.prepare("insert into jaxcon.lines (book, lineNum, line) values ('macbeth',?,?)");

        BiFunction<Values, PreparedStatement, BoundStatement> statementBinder =
                (values, statement) ->
                        statement.bind(values.lineNum, values.line);

        final Path file = Paths.get("macbeth.txt");

        Sink<Values, CompletionStage<Done>> printlnSink =
                Sink.<Values>foreach(values ->
                        System.out.println(values.lineNum + ": " + values.line));

        final Sink<Values, CompletionStage<Done>> sink =
                CassandraSink.create(1, preparedStatement, statementBinder, session);

        final Source<String, CompletionStage<IOResult>> linesSource =
                FileIO.fromPath(file)
                        .via(Framing.delimiter(ByteString.fromString("\n"), 255))
                        .map(ByteString::utf8String);

        final Source<Integer, NotUsed> numberSource = Source.range(1, 1000000);

        numberSource
                .buffer(150, OverflowStrategy.backpressure())
                .zipWith(linesSource, (lineNum, line) -> new Values(lineNum, line))
                .wireTap(printlnSink)
                .runWith(sink, mat)
                .whenComplete((result, ex) -> system.terminate());
    }
}

class Values {
    public final int lineNum;
    public final String line;

    public Values(int lineNum, String line) {
        this.lineNum = lineNum;
        this.line = line;
    }
}

/*
CREATE KEYSPACE jaxcon WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE lines (book varchar, lineNum int, line varchar, primary key (book, lineNum));
 */
