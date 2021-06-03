package me.zeab.tvtuner.features.video

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.MediaType.WithFixedCharset
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import me.zeab.tvtuner.shared.Get

import java.util.UUID
import scala.concurrent.duration.DurationInt

trait Video extends Directives {

  val `video/ts`: WithFixedCharset =
    MediaType.customWithFixedCharset("video", "ts", HttpCharsets.`UTF-8`)

  //Part of the TvTuner API were mocking
  def video(testChannel: ActorRef): Route =
    extractActorSystem { implicit system =>
      extractExecutionContext { implicit ec =>
        path("video") {
          get {
            parameters("channel") { channelNumber: String =>

              implicit val timeout: Timeout = Timeout(30.second)

              val uuid: UUID = UUID.randomUUID()

              //TODO I need to find the channel by the id that is given right now i just pass back the test channel regardless

              val channel: Source[ByteString, NotUsed] =
                Source.repeat(Get)
                  .throttle(
                    1,
                    500.millisecond
                  )
                  .ask[Seq[ByteString]](parallelism = 1)(testChannel)
                  .map { byteStrings: Seq[ByteString] => ByteString(byteStrings.flatten: _*) }

              complete(
                HttpResponse(
                  StatusCodes.OK,
                  entity = HttpEntity(`video/ts`, channel)
                )
              )
            }
          }
        }
      }
    }

}
