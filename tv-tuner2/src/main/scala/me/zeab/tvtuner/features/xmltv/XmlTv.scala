package me.zeab.tvtuner.features.xmltv

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import me.zeab.tvtuner.shared.Get

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.xml.{Elem, PrettyPrinter}

trait XmlTv extends Directives {

  def xmlTv(xmlTvManager: ActorRef): Route = {
    extractExecutionContext { implicit ec: ExecutionContext =>
      path("xmltv") {
        get {

          implicit val timeout: Timeout = Timeout(5.second)

          complete(
            HttpResponse(
              entity = HttpEntity(
                ContentType(
                  MediaTypes.`application/xml`, HttpCharsets.`UTF-8`
                ),
                Source.single(Get)
                  .ask[Elem](1)(xmlTvManager)
                  .map { xmlTv: Elem =>
                    val prettyPrinter: PrettyPrinter = new scala.xml.PrettyPrinter(200, 2)
                    val prettyXml: String = prettyPrinter.format(xmlTv)
                    ByteString(prettyXml)
                  }
              )
            )
          )
        }
      }
    }
  }

}
