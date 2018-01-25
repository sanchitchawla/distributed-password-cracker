name := "bigData-a0"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.4"
)
libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"