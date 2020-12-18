package com.zeab.tvturner2.feature.xmltv

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util

import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes, _}
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.channel.model.{SourceInfo, Channel => ChannelDefinition}
import com.zeab.tvturner2.feature.xmltv.models.{Channel, Programme, Schedule, Tv}
import com.zeab.tvturner2.service.{AppConf, DateTimeFormatters, FileHelpers}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.probe.FFmpegProbeResult

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Random, Success, Try}

trait XmlTv extends Directives with FileHelpers with DateTimeFormatters {

  val ffprobe: FFprobe

  def get48Hours(channelId: String, files: List[File]): List[(Programme, Schedule)] = {
    val random: Random = new Random

    @tailrec
    def worker(channelId: String, files: List[File], totalFiles: List[(Programme, Schedule)], duration: Double): List[(Programme, Schedule)] = {
      if (duration >= 172800) totalFiles
      else {
        val mediaItem: File = files(random.nextInt(files.length))
        val probeResult: FFmpegProbeResult = ffprobe.probe(mediaItem.getAbsolutePath)

        val now: ZonedDateTime = {
          if (totalFiles.isEmpty) ZonedDateTime.now()
          else ZonedDateTime
            .parse(
              totalFiles.lastOption.getOrElse(throw new Exception("some how we dont have anything in the schedule")
              )._2.endTime, standardDateTimeFormat)
        }

        val startTime: String = now.format(standardDateTimeFormat)
        val endTime: String = now.plusSeconds(probeResult.format.duration.toLong).format(standardDateTimeFormat)

        //TODO Using http calls out to tv db or movies try and grab the info about this episode and paste it here

        val programme: Programme = Programme(startTime, endTime, channelId, mediaItem.getName, "en", "subtitle", "desc")
        val schedule: Schedule = Schedule(startTime, endTime, mediaItem.getAbsolutePath, channelId)

        worker(channelId, files, totalFiles ++ List(programme -> schedule), duration + probeResult.format.duration)
      }
    }

    worker(channelId, files, List.empty, 0)
  }

  val xmlTv: Route =
    path("xmltv") {
      get {
        val (channels, programSchedule): (List[Channel], List[(Programme, Schedule)]) =
          Try(Files.readAllLines(Paths.get(AppConf.channelsPath))) match {
            case Failure(exception: Throwable) => throw exception
            case Success(lines: util.List[String]) =>
              decode[List[ChannelDefinition]](lines.asScala.toList.mkString) match {
                case Left(exception: circe.Error) => throw exception
                case Right(channels: List[ChannelDefinition]) =>
                  val (completedChannel: List[Channel], completedProgrammeSchedule: List[List[(Programme, Schedule)]]) =
                    channels.map { channel: ChannelDefinition =>
                      val availableFiles: List[File] =
                        channel.sources.flatMap { source: SourceInfo =>
                          listAllFiles(new File(source.uri))
                        }
                      Channel(channel.id, channel.name) -> get48Hours(channel.id, availableFiles)
                    }.unzip
                  completedChannel -> completedProgrammeSchedule.flatten
              }
          }

        //Write the xmltv.xml file
        val tv: String = Tv("tv-tuner", channels, programSchedule.map(_._1)).toXml.toString
        writeFile(new File(AppConf.xmlTvPath), tv)

        //Write the schedule.json file
        val schedule: String = programSchedule.map(_._2).asJson.toString
        writeFile(new File(AppConf.schedulePath), schedule)

        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), tv.toString())))
      }
    }

}
