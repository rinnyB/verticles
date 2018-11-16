package verticles

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.{Future, Promise}
import scala.util.{Success, Failure}

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.kafka.client.consumer.{KafkaConsumer, KafkaConsumerRecord}


class KafkaVerticle extends ScalaVerticle {

  val name: String = "KafkaVerticle" + this.toString.split('@')(1)  

  override def startFuture(): Future[Unit] = {

    println(s"Starting Kafka Verticle $name")
    
    val data = vertx
        .sharedData()
        .getLocalMap[String, String]("kafkaConfig")

    val config: MMap[String, String] = MMap(
        ("bootstrap.servers", data.get("bootstrap.servers")),
        ("key.deserializer", data.get("key.deserializer")),
        ("value.deserializer", data.get("value.deserializer")),
        ("group.id", data.get("group.id")),
        ("auto.offset.reset", data.get("auto.offset.reset")),
        ("enable.auto.commit", data.get("enable.auto.commit"))
    )

    val consumer = KafkaConsumer.create[String, String](vertx, config)
    consumer.subscribe("OUT")

    consumer.handler(
        {record => vertx.eventBus().sendFuture[Event]("topix", Event(record.key, record.value))}
    )
        Future.successful(())
  }

  override def stopFuture(): Future[Unit] = {
    println("Stopping Kafka Verticle")
    Future.successful(())
  }
}