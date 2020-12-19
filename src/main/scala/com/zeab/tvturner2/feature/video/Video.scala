package com.zeab.tvturner2.feature.video

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.MediaType.WithFixedCharset
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.zeab.tvturner2.feature.channel.model.Get

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

trait Video extends Directives {

  val utf8: HttpCharset = HttpCharsets.`UTF-8`
  val `video/ts`: WithFixedCharset =
    MediaType.customWithFixedCharset("video", "ts", utf8)

  //How do we send 1 second of video
  //How do we determine 1 second of video

  //Part of the TvTuner API were mocking
  val video: Route =
    extractActorSystem { implicit system =>
      extractExecutionContext { implicit ec =>
        path("video") {
          get {
            parameters("channel") { channelNumber: String =>
              implicit val timeout: Timeout = Timeout(30.second)

              println(s"looking for actor user/Channel$channelNumber")
              onComplete(system.actorSelection(s"user/Channel$channelNumber").resolveOne()) {
                case Success(actorRef: ActorRef) =>
                  val channelSource: Source[ByteString, NotUsed] =
                    Source.repeat(Get)//.throttle(60, 1.second)
                      .throttle(
                        1,
                        9999999.second
                      )
                      .map { ss =>
                        println(s"moose ${UUID.randomUUID()}")
                        ss
                      }
                      .ask[ByteString](parallelism = 1)(actorRef)

                  complete(
                    HttpResponse(
                      StatusCodes.OK,
                      entity = HttpEntity(`video/ts`, channelSource)
                    )
                  )
                case Failure(_) =>
                  println("cant find the actor")
                  complete(
                    HttpResponse(
                      StatusCodes.InternalServerError
                    )
                  )
              }
            }
          }
        }
      }
    }

}
