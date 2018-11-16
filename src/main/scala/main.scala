package verticles

import io.vertx.scala.core.Vertx
import io.vertx.scala.core.VertxOptions
import io.vertx.scala.core.DeploymentOptions
import io.vertx.scala.core.eventbus.{Message, DeliveryOptions}
import io.vertx.core.eventbus.{EventBus => JEventBus}
import io.vertx.core.json.Json

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import scala.util.{Success, Failure}


import codecs.EventCodec

object vert {

  def main(args: Array[String]): Unit = {

  implicit val ec = ExecutionContext.global
    val opts = VertxOptions().setWorkerPoolSize(5)
    val vertx = Vertx.vertx(opts)

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
