package verticles

import scala.concurrent.{Future, Promise}

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.kafka.client.consumer.{KafkaConsumer, KafkaConsumerRecord}

import scala.collection.mutable.{Map => MMap}

class ClientVerticle extends ScalaVerticle {

  val name: String = "Verticle" + this.toString.split('@')(1)

  override def startFuture(): Future[Unit] = {
    println(s"Starting Client $name")
    val consumer = vertx.eventBus().consumer[Event]("topix")
    consumer.handler({e => println(s"name: $name"); println(e.body()); e.reply("DANK")})
    Future.successful(())    
  }

  override def stopFuture(): Future[Unit] = {
    println("Stopping")
    Future.successful(())
  }
}

class KafkaVerticle extends ScalaVerticle {

  val name: String = "KafkaVerticle" + this.toString.split('@')(1)  

  override def startFuture(): Future[Unit] = {

  println("Starting Kafka Verticle $name")

  val config: MMap[String, String] = MMap(
    ("bootstrap.servers" -> "localhost:9092"), 
    ("key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"),
    ("value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"),
    ("group.id" -> "my_group"),
    ("auto.offset.reset" -> "latest"),
    ("enable.auto.commit" -> "false")
  )
  val consumer = KafkaConsumer.create[String, String](vertx, config)
  consumer.subscribe("OUT")

  consumer.handler({ 
      record => vertx.eventBus().sendFuture[Event]("topix", Event(record.key, record.value))
  })
    Future.successful(())
  }

    override def stopFuture(): Future[Unit] = {
    println("Stopping Kafka Verticle")
    Future.successful(())
  }
}