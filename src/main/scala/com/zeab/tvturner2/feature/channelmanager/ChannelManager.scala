package com.zeab.tvturner2.feature.channelmanager

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util
import java.util.UUID

import akka.actor.{Actor, Props}
import akka.stream.Materializer
import com.zeab.tvturner2.feature.xmltv.models.Schedule
import com.zeab.tvturner2.service.{AppConf, DateTimeFormatters, FFmpegStuff, FileHelpers}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.collection.immutable
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Success, Try}
//when i start the applicaiton i need to start one of these
//it will read though the schedule and figure out what w

//TODO create a sorta like refresher ondecker thingy where it looks for the next program
//and removes the last packet from the queue and splices it together which this one... is that something i have to do
//idk ... i just dont know yet ...

class ChannelManager(implicit mat: Materializer) extends Actor with FFmpegStuff with DateTimeFormatters with FileHelpers{

  def receive: Receive = {
    case Start =>

      //TODO Make this better...
      //Make the temp dir and delete all the files there
      val temp = new File(AppConf.tempPath)
      temp.mkdir()
      listAllFiles(temp).map(_.delete())

      Try(Files.readAllLines(Paths.get(AppConf.schedulePath))) match {
        case Failure(exception: Throwable) => throw exception
        case Success(lines: util.List[String]) =>
          decode[List[Schedule]](lines.asScala.toList.mkString) match {
            case Left(exception: circe.Error) => throw exception
            case Right(channels: List[Schedule]) =>
              val now: ZonedDateTime = ZonedDateTime.now()

              val onDeckItems: immutable.Iterable[Schedule] =
                channels.filter { schedule: Schedule =>
                  now.isAfter(ZonedDateTime.parse(schedule.startTime, standardDateTimeFormat)) &
                    now.isBefore(ZonedDateTime.parse(schedule.endTime, standardDateTimeFormat))
                }

              onDeckItems.foreach { item: Schedule =>
                val channelName: String = s"Channel${item.channelId}"
                println(s"starting the channels $channelName")
                context.system.actorOf(Props(classOf[ChannelEmitter], mat), channelName) ! item.uri
              }

          }
      }

  }

  override def preStart(): Unit = {
    self ! Start
  }

  case object Start

}
