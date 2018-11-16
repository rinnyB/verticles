package verticles

import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.Json
import io.vertx.lang.scala.json.JsonObject

class EventCodec extends MessageCodec[Event, Event] {
  override def encodeToWire(buffer: Buffer, event: Event): Unit = {
    // make a json object from event
    val json: JsonObject = new JsonObject()
    json.put("key", event.key).put("value", event.value)
    // convert json to string
    val jsonStr: String = json.encode()
    val length: Int = jsonStr.getBytes().length
    // put length and string to buffer
    buffer.appendInt(length)
    buffer.appendString(jsonStr)
  }

  override def decodeFromWire(pos: Int, buffer: Buffer): Event = {
    // get message from buffer

    val length: Int = buffer.getInt(pos)
    val start: Int = pos + 4
    val jsonStr: String = buffer.getString(start, start + length)

    val json: JsonObject = new JsonObject(jsonStr)

    val key = json.getString("key")
    val value = json.getString("value")

    Event(key, value)
  }

  override def transform(ev: Event): Event = {
    // send message locally across event bus
    // aka not in cluster
    ev
  }

  override def name(): String = {
    // unique name of codec
    this.getClass().getSimpleName()
  }

  override def systemCodecID(): Byte = {
    // user def codecs must always return -1
      -1
  }
}