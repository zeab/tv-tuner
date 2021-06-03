package me.zeab.tvtuner.features.catalog

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import me.zeab.tvtuner.features.catalog.models.CatalogItem
import me.zeab.tvtuner.service.{Marshallers, Unmarshallers}
import me.zeab.tvtuner.shared.Get

import scala.concurrent.duration.DurationInt

trait Catalog extends Directives with Marshallers with Unmarshallers {

  implicit val timeout: Timeout = Timeout(5.second)

  def catalog(catalogManager: ActorRef): Route =
    path("catalog") {
      get {
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(
              ContentType(MediaTypes.`application/json`),
              Source.single(Get)
                .ask[Seq[CatalogItem]](1)(catalogManager)
                .map { catalogItems: Seq[CatalogItem] =>
                  ByteString(catalogItems.asJson.noSpaces)
                }
            )
          )
        )
      } ~
        post {
          decodeRequest {
            entity(as[Seq[CatalogItem]]) { catalogItems: Seq[CatalogItem] =>
              catalogItems.foreach((catalogItem: CatalogItem) => catalogManager ! catalogItem)
              complete(StatusCodes.Accepted, "Catalog items being added to catalog")
            }
          }
        }
    }

}
