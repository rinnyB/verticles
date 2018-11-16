package verticles

import scala.concurrent.{Future, Promise}
import scala.util.{Success, Failure}

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}


class ClientVerticle extends ScalaVerticle {

  val name: String = "Verticle" + this.toString.split('@')(1)

  override def startFuture(): Future[Unit] = {
    println(s"Starting Client $name")
    val clientOptions = WebClientOptions()
      .setUserAgent(name)
      .setTrustAll(true)
    val client = WebClient.create(vertx, clientOptions)
    val consumer = vertx.eventBus().consumer[Event]("topix")
    consumer.handler(
      {
        event => client
          .postAbs("https://localhost:6000/")
          .ssl(true)
          .sendJsonObjectFuture {
            new JsonObject().put("key", event.body.key).put("value", event.body.value)
          }
      }
    )
    Future.successful(())
  }

  override def stopFuture(): Future[Unit] = {
    println("Stopping")
    Future.successful(())
  }
}