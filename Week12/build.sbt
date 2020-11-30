organization := "com.lightbend.akka.samples"
name := "akka-sample-persistence-scala"

scalaVersion := "2.13.1"

val akkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.1"
val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,

  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",

//  "com.typesafe.akka" %% "akka-slf4j"             % "2.6.1",
//  "com.typesafe.akka" %% "akka-http-spray-json"   % "10.1.11",
//  "com.pauldijou"    %% "jwt-core"               % "4.2.0",
//  "com.pauldijou"    %% "jwt-json4s-jackson"     % "4.2.0",
//  "com.pauldijou"    %% "jwt-json4s-native"      % "4.2.0"
)

scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")

// show full stack traces and test case durations
testOptions in Test += Tests.Argument("-oDF")
logBuffered in Test := false

licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
