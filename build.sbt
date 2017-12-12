name := "hello-postgres"

version := "0.1"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.4"
val akkaHttpVersion = "10.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.json4s" %% "json4s-native" % "3.6.0-M2"
)