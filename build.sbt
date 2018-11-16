scalaVersion := "2.12.7"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.rinnyb"
Global / cancelable := true

val vertx_scala = "io.vertx" %% "vertx-lang-scala" % "3.5.4"
val vertx_kafka = "io.vertx" %% "vertx-kafka-client-scala" % "3.5.4"
val vertx_web = "io.vertx" %% "vertx-web-client-scala" % "3.5.4"

lazy val hello = (project in file("."))
    .settings(
        name := "vertx",
        libraryDependencies ++= Seq(
            vertx_scala, vertx_kafka, vertx_web),
        fork in run := true
    )