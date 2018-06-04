name := "offers-app"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

val AkkaHttpVersion = "10.1.1"
val AkkaVersion = "2.5.13"
val ScalaTestVersion = "3.0.5"
val ScalaMockVersion = "4.1.0"
val JodaTimeVersion = "2.10"
val ScaffeineVersion = "2.3.0"
val Log4jScalaApiVersion = "11.0"
val Log4jVersion = "2.9.1"
val TypeSafeConfigVersion = "1.3.3"
val JodaMoneVersion = "0.12"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % TypeSafeConfigVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "joda-time" % "joda-time" % JodaTimeVersion,
  "org.joda" % "joda-money" % JodaMoneVersion,
  "com.github.blemale" %% "scaffeine" % ScaffeineVersion,
  "org.apache.logging.log4j" %% "log4j-api-scala" % Log4jScalaApiVersion,
  "org.apache.logging.log4j" % "log4j-api" % Log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
  "org.scalamock" %% "scalamock" % ScalaMockVersion % Test
)

parallelExecution in Test := false