name := "tv-tuner"

version := "0.1"

scalaVersion := "2.12.8"



libraryDependencies += "net.bramp.ffmpeg" % "ffmpeg" % "0.6.2"

libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "io.resourcepool" % "ssdp-client" % "2.4.1"
libraryDependencies += "org.matthicks" %% "media4s" % "1.0.15"

//Akka
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.10"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.6.10"
//Akka-Http
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.13"
//Json
libraryDependencies += "io.circe" %% "circe-parser" % "0.11.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.11.1"
//Logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "6.1"
//Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"