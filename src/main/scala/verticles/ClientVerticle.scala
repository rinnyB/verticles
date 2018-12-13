package verticles

import scala.concurrent.{Future, Promise}
import scala.util.{Success, Failure}

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}

import io.vertx.micrometer.backends.BackendRegistries

class ClientVerticle extends ScalaVerticle {

  val name: String = "Verticle" + this.toString.split('@')(1)

  override def startFuture(): Future[Unit] = {

    val registry = BackendRegistries.getDefaultNow();
    val sentGood = registry.counter("sentGood")
    val sentBad = registry.counter("sentBad")

    println(s"Starting Client Verticle $name")
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
          }.onComplete {
            case Success(resp) => sentGood.increment
            case Failure(ex) => sentBad.increment
          }
      }
    )
    Future.successful(())
  }

  override def stopFuture(): Future[Unit] = {
    println(s"Stopping Client Verticle $name")    
    Future.successful(())
  }
}