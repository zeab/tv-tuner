package com.zeab.tvturner2.feature.channelmanager

import java.io.File

import akka.Done
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.zeab.tvturner2.feature.channel.model.Get
import com.zeab.tvturner2.feature.xmltv.models.Schedule
import com.zeab.tvturner2.service.{AppConf, FFmpegStuff, FileHelpers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ChannelEmitter2(implicit mat: Materializer) extends Actor with FileHelpers with FFmpegStuff {

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = queue()

  def queue(data: List[MediaPiece] = List.empty): Receive = {
    case incomingData: MediaPiece =>
      context.become(queue(data ++ List(incomingData)))
    case schedule: Schedule =>
      split(new File(schedule.uri))

      val filesToLoad: List[File] =
        listAllFiles(AppConf.tempPath)
          .filter(_.getName.contains(new File(schedule.uri).getName))
          .toList

      self ! filesToLoad
    case files: List[File] =>
      files.headOption match {
        case Some(file: File) =>

          val fileOpener: ActorRef =
            context.actorOf(Props(classOf[FileOpener], file.getName))

          FileIO
            .fromPath(file.toPath)
            .runForeach { item: ByteString => fileOpener ! item }
            .onComplete { _ =>
              fileOpener ! Done
              self ! files.drop(1)
              file.delete()
            }
        case None =>
          println(s"everything is loaded ${data.size}")
      }
    case Get =>
      data.headOption match {
        case Some(mediaPiece) =>
          sender() ! mediaPiece.data
          context.become(queue(data.drop(1)))
        case None =>
          println("we have no media pieces to give so thats a problem...")
      }
  }

}
