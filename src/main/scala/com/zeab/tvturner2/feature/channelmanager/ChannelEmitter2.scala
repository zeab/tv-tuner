package com.zeab.tvturner2.feature.channelmanager

import java.io.File
import java.time.ZonedDateTime

import akka.Done
import akka.actor.{Actor, ActorRef, Cancellable, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.zeab.tvturner2.feature.channel.model.Get
import com.zeab.tvturner2.feature.xmltv.models.Schedule
import com.zeab.tvturner2.service.{AppConf, DateTimeFormatters, FFmpegStuff, FileHelpers}
import net.bramp.ffmpeg.FFprobe

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ChannelEmitter2(implicit mat: Materializer) extends Actor with FileHelpers with FFmpegStuff with DateTimeFormatters {

  val ffprobe: FFprobe = new FFprobe(AppConf.ffprobePath)

  implicit val ec: ExecutionContext = context.system.dispatcher

  val refresher: Cancellable =
    context.system.scheduler.scheduleOnce(5.second){self ! Refresh}

  def receive: Receive = queue()

  def queue(data: List[MediaPiece] = List.empty): Receive = {
    case Refresh =>
      data.headOption match {
        case Some(mediaPiece) =>
          val now: ZonedDateTime = ZonedDateTime.now()
          if (now.isBefore(mediaPiece.endDateTime)) println("data is fine")
            //context.become(queue(data ++ List(incomingData)))
          else {
            context.become(queue(data.drop(1)))
            println("data was dropped")
          }
        case None =>
      }
      context.system.scheduler.scheduleOnce(5.second){self ! Refresh}
    case incomingData: MediaPiece =>
      val now: ZonedDateTime = ZonedDateTime.now()
      if (now.isBefore(incomingData.endDateTime))
        context.become(queue(data ++ List(incomingData)))
      else
        println("data was dropped")
    case schedule: Schedule =>
      split(new File(schedule.uri))

      val startDateTime: ZonedDateTime =
        ZonedDateTime.parse(schedule.startTime, standardDateTimeFormat)

      val filesToLoad: List[(File, Double)] =
        listAllFiles(AppConf.tempPath)
          .filter(_.getName.contains(new File(schedule.uri).getName))
          .toList
          .map{ file: File =>
            file -> ffprobe.probe(file.getAbsolutePath).format.duration
          }

      self ! doTheThing(filesToLoad , startDateTime)
    case files: List[(File, Double, ZonedDateTime, ZonedDateTime)] =>
      files.headOption match {
        case Some(files1@(file, duration, startDateTime, endDateTime)) =>
          val fileOpener: ActorRef =
            context.actorOf(Props(classOf[FileOpener], file.getName, duration, startDateTime, endDateTime))

          FileIO
            .fromPath(file.toPath)
            .runForeach { item: ByteString => fileOpener ! item }
            .onComplete { _ =>
              fileOpener ! Done
              self ! files.drop(1)
              //file.delete()
            }
        case None =>
          println(s"everything is loaded ${data.size}")
      }
    case Get =>
      data.headOption match {
        case Some(mediaPiece: MediaPiece) =>
          sender() ! mediaPiece.data
          context.become(queue(data.drop(1)))
        case None =>
          println("we have no media pieces to give so thats a problem...")
      }
  }

  def doTheThing(files: List[(File, Double)], startDateTime: ZonedDateTime): List[(File, Double, ZonedDateTime, ZonedDateTime)] ={

    @tailrec
    def worker(files: List[(File, Double)], startDateTime: ZonedDateTime, mediaPieces: List[(File, Double, ZonedDateTime, ZonedDateTime)] = List.empty): List[(File, Double, ZonedDateTime, ZonedDateTime)] ={
      if (files.isEmpty) mediaPieces
      else {
          files.headOption match {
            case Some(value@(file, duration)) =>
              val endDateTime: ZonedDateTime = startDateTime.plusSeconds(duration.toLong)
              val newPiece: List[(File, Double, ZonedDateTime, ZonedDateTime)] = List((file, duration, startDateTime, endDateTime))
              worker(files.drop(1), endDateTime, mediaPieces ++ newPiece)
            case None => throw new Exception("wtf")
          }
      }
    }

    worker(files, startDateTime)
  }

  case object Refresh

}
