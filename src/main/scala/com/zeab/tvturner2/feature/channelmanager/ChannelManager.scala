package com.zeab.tvturner2.feature.channelmanager

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.zeab.tvturner2.feature.xmltv.models.Schedule
import com.zeab.tvturner2.service.{AppConf, DateTimeFormatters, FFmpegStuff, FileHelpers}
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Success, Try}
//when i start the applicaiton i need to start one of these
//it will read though the schedule and figure out what w

//TODO create a sorta like refresher ondecker thingy where it looks for the next program
//and removes the last packet from the queue and splices it together which this one... is that something i have to do
//idk ... i just dont know yet ...

//TODO now i need a thing that reads the scduelde again in a while and loads the next item

class ChannelManager(implicit mat: Materializer) extends Actor with FFmpegStuff with DateTimeFormatters with FileHelpers {

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = queue()

  def queue(channels: List[ActorRef] = List.empty, currentItems: List[Schedule] = List.empty): Receive = {
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
            case Right(schedules: List[Schedule]) =>
              val now: ZonedDateTime = ZonedDateTime.now()

              val onDeckItems: List[Schedule] =
                schedules.filter { schedule: Schedule =>
                  now.isAfter(ZonedDateTime.parse(schedule.startTime, standardDateTimeFormat)) &
                    now.isBefore(ZonedDateTime.parse(schedule.endTime, standardDateTimeFormat))
                }

              if (onDeckItems == currentItems) {
                println("the on deck is still current waiting another few min")
              }
              else {
                val newChannels: List[ActorRef] =
                  onDeckItems.map { item: Schedule =>
                    val channelName: String = s"Channel${item.channelId}"
                    println(s"starting the channels $channelName")
                    channels.find(_.path.name == channelName) match {
                      case Some(foundChannel: ActorRef) =>
                        foundChannel ! item
                        foundChannel
                      case None =>
                        val channel: ActorRef = context.system.actorOf(Props(classOf[ChannelEmitter2], mat), channelName)
                        channel ! item
                        channel
                    }
                  }
                context.become(queue(newChannels, onDeckItems))
              }
              context.system.scheduler.scheduleOnce(300.second) {
                self ! Start
              }
          }
      }
  }


  override def preStart(): Unit = {
    self ! Start
  }

  case object Start

}
