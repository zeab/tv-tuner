package com.zeab.tvturner2.feature.xmltv

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes, _}
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.channel.model.{Channel => ChannelDefinition}
import com.zeab.tvturner2.feature.xmltv.models.{Channel, Programme, Schedule, Tv}
import com.zeab.tvturner2.service.FileHelpers
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import net.bramp.ffmpeg.FFprobe

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Random, Success, Try}

trait XmlTv extends Directives with FileHelpers {

  val ffprobe: FFprobe

  val dateformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

  def get48Hours(files: List[File], channelId: String): List[(Programme, Schedule)] = {
    val random = new Random
    @tailrec
    def worker(files: List[File], totalFiles: List[(Programme, Schedule)], duration: Double, channelId: String): List[(Programme, Schedule)] = {
      if (duration >= 172800) totalFiles
      else {
        val mediaItem = files(random.nextInt(files.length))
        val probeResult = ffprobe.probe(mediaItem.getAbsolutePath)
        val gg = probeResult.getFormat

        val now: ZonedDateTime = {
          if (totalFiles.isEmpty) ZonedDateTime.now()
          else LocalDateTime.parse(totalFiles.last._2.endTime.replace("+0000", "").trim, dateformatter).atZone(ZoneId.of("America/Los_Angeles"))
        }

        val startTime = now.format(dateformatter) + " +0000"
        val endTime = now.plusSeconds(gg.duration.toLong).format(dateformatter) + " +0000"

        //Using http calls out to tv db or movies try and grab the info about this episode and paste it here

        val fileDuration: Double = gg.duration
        val programmes = Programme(startTime, endTime, channelId, "title", "lang", "subtitle", "desc")
        val schedule = Schedule(startTime, endTime, mediaItem.getAbsolutePath, channelId)

        println(duration + fileDuration)
        worker(files, totalFiles ++ List(programmes -> schedule), duration + fileDuration, channelId)
      }
    }

    worker(files, List.empty, 0, channelId)
  }

  val xmlTv: Route =
    path("xmltv") {
      get {
        val ll =
          Try(Files.readAllLines(Paths.get("channels.json"))) match {
            case Failure(exception) => throw exception
            case Success(lines) =>
              decode[List[ChannelDefinition]](lines.asScala.toList.mkString) match {
                case Left(exception) => throw exception
                case Right(channels) =>
                  val xx =
                    channels.map { channel =>

                      val availableFiles =
                        channel.sources.flatMap { source =>
                          listAllFiles(new File(source.uri))
                        }

                      Channel(channel.id, channel.name) -> get48Hours(availableFiles, channel.id)
                    }

                  val kk = xx.unzip
                  kk._1 -> kk._2.flatten
              }
          }
        val tv = Tv("tv-tuner", ll._1, ll._2.map(_._1)).toXml

        writeFile(new File("xmltv.xml"), tv.toString())

        val schedule = ll._2.map(_._2).asJson.toString
        writeFile(new File("schedule.json"), schedule)

        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), tv.toString())))
      }
    }

}
