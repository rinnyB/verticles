package verticles

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import io.vertx.scala.core.Vertx
import io.vertx.scala.core.VertxOptions
import io.vertx.scala.core.DeploymentOptions
import io.vertx.scala.core.eventbus.{Message, DeliveryOptions}

import io.vertx.core.http.HttpServerOptions
import io.vertx.core.{Vertx => JVertx}
import io.vertx.core.eventbus.{EventBus => JEventBus}
import io.vertx.core.json.Json

import io.vertx.micrometer.backends.BackendRegistries
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.PrometheusScrapingHandler
import io.vertx.micrometer.VertxPrometheusOptions

import io.micrometer.core.instrument.{MeterRegistry, Counter}



object vert {

  def main(args: Array[String]): Unit = {

    implicit val ec = ExecutionContext.global
  
    val options: MicrometerMetricsOptions = new MicrometerMetricsOptions()
      .setPrometheusOptions(new VertxPrometheusOptions()
        .setStartEmbeddedServer(true)
        .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
        .setEnabled(true)
      ).setEnabled(true)

    val opts = VertxOptions().asJava
      .setMetricsOptions(options)
      .setWorkerPoolSize(5)

    val jvertx = JVertx.vertx(opts)
    val vertx = Vertx(jvertx)

    val registry = BackendRegistries.getDefaultNow();

    Counter.builder("recv").description("Messages received by Kafka Consumer").register(registry)
    Counter.builder("sentBad").description("Messages not sent by WebClient(s)").register(registry)
    Counter.builder("sentGood").description("Messages sent by WebClient(s)").register(registry)

    val data = vertx
      .sharedData()
      .getLocalMap[String, String]("kafkaConfig")
    
    data.put("bootstrap.servers", "localhost:9092")
    data.put("key.deserializer" , "org.apache.kafka.common.serialization.StringDeserializer")
    data.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    data.put("group.id", "my_group")
    data.put("auto.offset.reset", "latest")
    data.put("enable.auto.commit", "false")
    
    val kafkaDeployment = vertx.deployVerticleFuture(
      s"scala:${classOf[KafkaVerticle].getName}", 
      DeploymentOptions().setWorker(true).setInstances(1)
    )
    val clientDeployment = vertx.deployVerticleFuture(
      s"scala:${classOf[ClientVerticle].getName}", 
      DeploymentOptions().setWorker(true).setInstances(4)
    )

    clientDeployment.onComplete {
      case Success(re) => println(s"$re is up and running!")
      case Failure(ex) => println(ex)
    }
    kafkaDeployment.onComplete {
      case Success(re) => println(s"$re is up and running!")
      case Failure(ex) => println(ex)
    }

    val codec = new EventCodec()

    vertx.eventBus
      .asJava.asInstanceOf[JEventBus]
      .registerDefaultCodec(classOf[Event], codec)
    



    sys.ShutdownHookThread {
      vertx.close()
      println("DOWN")
    }
  }
}
