package com.zeab.tvturner2.feature.lineup

import java.nio.file.{Files, Paths}
import java.util

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.channel.model.{Channel => ChannelDefinition}
import com.zeab.tvturner2.feature.lineup.models.{LineUpStatus, LineupChannel}
import com.zeab.tvturner2.service.{AppConf, FileHelpers}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Success, Try}

trait Lineup extends Directives with FileHelpers {

  //Part of the TvTuner API were mocking
  val lineupStatusJson: Route =
    path("lineup_status.json") {
      get {
        val lineupStatus: String = LineUpStatus().asJson.noSpaces
        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), lineupStatus)))
      }
    }

  //Part of the TvTuner API were mocking
  val lineupJson: Route =
    path("lineup.json") {
      get {
        val lineup: String =
          Try(Files.readAllLines(Paths.get(AppConf.channelsPath))) match {
            case Failure(exception: Throwable) => throw exception
            case Success(lines: util.List[String]) =>
              decode[List[ChannelDefinition]](lines.asScala.toList.mkString) match {
                case Left(exception: circe.Error) => throw exception
                case Right(channels: List[ChannelDefinition]) =>
                  channels
                    .map { channel: ChannelDefinition => LineupChannel(channel.id, channel.name, channel.url) }
                    .asJson
                    .toString()
              }
          }
        complete(
          HttpResponse(
            StatusCodes.OK,
            entity = HttpEntity(
              ContentType(MediaTypes.`application/json`),
              lineup
            )
          )
        )
      }
    }

}
