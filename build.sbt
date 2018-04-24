name := """panta-rhei"""

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "io.reactivex.rxjava2" % "rxjava" % "2.1.12",
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.18"
)


addCommandAlias("ex1", "runMain eu.reactivesystems.pantarhei.example1.Example1")
addCommandAlias("ex2", "runMain eu.reactivesystems.pantarhei.example2.Example2")
addCommandAlias("ex3", "runMain eu.reactivesystems.pantarhei.example3.Example3")
addCommandAlias("ex4", "runMain eu.reactivesystems.pantarhei.example4.Example4")
addCommandAlias("ex5", "runMain eu.reactivesystems.pantarhei.example5.Example5")

cancelable in Global := true
