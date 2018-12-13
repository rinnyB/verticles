ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.rinnyb"
Global / cancelable := true

val vertx_version = "3.6.0"
val vertx_scala = "io.vertx" %% "vertx-lang-scala" % vertx_version
val vertx_kafka = "io.vertx" %% "vertx-kafka-client-scala" % vertx_version
val vertx_client = "io.vertx" %% "vertx-web-client-scala" % vertx_version
val vertx_web = "io.vertx" %% "vertx-web-scala" % vertx_version
val vertx_metrics = "io.vertx" % "vertx-micrometer-metrics" % vertx_version

val micrometer = "io.micrometer" % "micrometer-registry-prometheus" % "1.1.0"

lazy val proj = (project in file("."))
  .settings(
    name := "vertx",
    libraryDependencies ++= Seq(vertx_scala, vertx_kafka, vertx_web,
                                vertx_metrics, vertx_client, micrometer),
    fork in run := true,
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
      case PathList("jni", xs @ _*)                      => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
      case "application.conf"                            => MergeStrategy.concat
      case "unwanted.txt"                                => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last endsWith ".json" => MergeStrategy.last
      case PathList(ps @ _*) if ps.last endsWith ".MF"   => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    mainClass in assembly := Some("verticle.server")
  );
