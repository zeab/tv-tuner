package me.zeab.tvtuner.features.lineup

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import me.zeab.tvtuner.features.lineup.models.{LineUpStatus, LineupItem}
import me.zeab.tvtuner.service.{FileHelpers, Marshallers, Unmarshallers}
import me.zeab.tvtuner.shared.Get

import scala.concurrent.duration.DurationInt

trait Lineup extends Directives with FileHelpers with Marshallers with Unmarshallers {

  //Part of the TvTuner API were mocking
  val lineupStatusJson: Route =
    path("lineup_status.json") {
      get {
        val lineupStatus: String = LineUpStatus().asJson.noSpaces
        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), lineupStatus)))
      }
    }

  implicit val timeout: Timeout = Timeout(5.second)

  //Part of the TvTuner API were mocking
  def lineupJson(lineupManager: ActorRef): Route =
    path("lineup.json") {
      get {
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(
              ContentType(MediaTypes.`application/json`),
              Source.single(Get)
                .ask[Seq[LineupItem]](2)(lineupManager)
                .map { xmlTv: Seq[LineupItem] =>
                  ByteString(xmlTv.asJson.noSpaces)
                }
            )
          )
        )
      } ~
        post{
          decodeRequest {
            entity(as[Seq[LineupItem]]) { lineupItems: Seq[LineupItem] =>
              lineupItems.foreach((lineupItem: LineupItem) => lineupManager ! lineupItem)
              complete(StatusCodes.Accepted, "Lineup items being added to lineup")
            }
          }
        }
    }

}
