name := "bigData-a0"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.4"
)
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.5"

libraryDependencies += "com.google.code.gson" % "gson" % "1.7.1"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.0-RC1"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1"

libraryDependencies += "io.relayr" % "rabbitmq-scala-client_2.11" % "0.1.8"
