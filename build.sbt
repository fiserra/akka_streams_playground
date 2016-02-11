name := "akka_streams_playground"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.3",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11"
)

    