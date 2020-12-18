package com.zeab.tvturner2.feature.channel

import java.io.File
import java.nio.file.{Files, Paths}
import java.util

import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.channel.model.{Channel => ChannelDefinition}
import com.zeab.tvturner2.service.{AppConf, FileHelpers, Unmarshallers}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Success, Try}

trait Channel extends Directives with FileHelpers with Unmarshallers {

  val channel: Route =
    path("channel") {
      get {
        val channels: String =
          Try(Files.readAllLines(Paths.get(AppConf.channelsPath))) match {
            case Failure(_) =>
              println("file not found so creating empty")
              val emptyJsonList: String = "[]"
              writeFile(new File(AppConf.channelsPath), emptyJsonList)
              emptyJsonList
            case Success(lines: util.List[String]) =>
              lines.asScala.toList.mkString
          }
        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), channels)))
      } ~
        post {
          decodeRequest {
            entity(as[List[ChannelDefinition]]) { req: List[ChannelDefinition] =>
              Try(Files.readAllLines(Paths.get(AppConf.channelsPath))) match {
                case Failure(_) =>
                  val emptyJsonList: String = "[]"
                  writeFile(new File(AppConf.channelsPath), emptyJsonList)
                  decode[List[ChannelDefinition]](emptyJsonList) match {
                    case Left(exception: circe.Error) => throw exception
                    case Right(channels: List[ChannelDefinition]) =>
                      writeFile(new File(AppConf.channelsPath), (channels ++ req).asJson.toString)
                  }
                case Success(lines: util.List[String]) =>
                  decode[List[ChannelDefinition]](lines.asScala.toList.mkString) match {
                    case Left(exception: circe.Error) => throw exception
                    case Right(channels: List[ChannelDefinition]) =>
                      writeFile(new File(AppConf.channelsPath), (channels ++ req).asJson.toString)
                  }
              }
              complete("file wrotten")
            }
          }
        } ~
        delete {
          decodeRequest {
            entity(as[List[Int]]) { req =>
              complete("to do on the delete")
            }
          }
        }
    }

}
