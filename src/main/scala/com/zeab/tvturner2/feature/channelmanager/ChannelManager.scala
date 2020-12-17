package com.zeab.tvturner2.feature.channelmanager

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import java.util.UUID

import akka.actor.{Actor, Props}
import akka.stream.Materializer
import com.zeab.tvturner2.feature.xmltv.models.Schedule
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.{Failure, Success, Try}
//when i start the applicaiton i need to start one of these
//it will read though the schedule and figure out what w

//TODO create a sorta like refresher ondecker thingy where it looks for the next program
//and removes the last packet from the queue and splices it together which this one... is that something i have to do
//idk ... i just dont know yet ...

class ChannelManager(implicit mat: Materializer) extends Actor with FFmpegStuff {

  val dateformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

  def receive: Receive = {
    case Start =>
      Try(Files.readAllLines(Paths.get("schedule.json"))) match {
        case Failure(exception: Throwable) => throw exception
        case Success(lines: util.List[String]) =>
          decode[List[Schedule]](lines.asScala.toList.mkString) match {
            case Left(exception: circe.Error) => throw exception
            case Right(channels: List[Schedule]) =>
              val now = LocalDateTime.now()
              val onDeckItems = channels.groupBy(_.channelId).flatMap { channel =>
                channel._2.filter { zz =>
                  now.isAfter(LocalDateTime.parse(zz.startTime.replace("+0000", "").trim, dateformatter)) &
                    now.isBefore(LocalDateTime.parse(zz.endTime.replace("+0000", "").trim, dateformatter))
                }
              }

              val fileCutNames =
                onDeckItems.map{item =>
                  val ident = UUID.randomUUID().toString
                  split(item.uri, ident)
                  ident
                }

              val c = 1 to fileCutNames.size
              val splicedTogether = fileCutNames.zip(c)
              splicedTogether.foreach{fileCutName =>
                println(s"starting the channels A${fileCutName._2}")
                context.system.actorOf(Props(classOf[ChannelEmitter], mat), s"A${fileCutName._2}") ! fileCutName._1
              }

              //Grab the items that we just created and upload it all
              onDeckItems.foreach(println)
          }
      }

  }

  override def preStart(): Unit = {
    self ! Start
  }

  case object Start

}
