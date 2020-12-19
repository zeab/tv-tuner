package com.zeab.tvturner2.feature.channelmanager

import java.io.File

import akka.actor.{Actor, ActorRef}
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import com.zeab.tvturner2.feature.channel.model.Get
import com.zeab.tvturner2.service.{AppConf, FFmpegStuff, FileHelpers}
import net.bramp.ffmpeg.FFprobe

import scala.concurrent.ExecutionContext

class ChannelEmitter(implicit mat: Materializer) extends Actor with FileHelpers with FFmpegStuff {

  val ffprobe: FFprobe = new FFprobe(AppConf.ffprobePath)

  implicit val ec: ExecutionContext = context.system.dispatcher

  val temp = new File(AppConf.tempPath)

  def receive: Receive = queue()

  //so now i need too timestamp the files when they come in
  //so when the emitter has them it can discard the ones we no longer need

  def queue(q: List[(String, Double, ByteString)] = List.empty): Receive = {
    case x: List[File] =>
      x.headOption match {
        case Some(file: File) =>

          val duration: Double = ffprobe.probe(file.getAbsolutePath).format.duration

          FileIO
            .fromPath(file.toPath)
            .runForeach { item: ByteString => self ! (file.getName, duration, item) }
            .onComplete{_ =>
              self ! x.drop(1)
              file.delete()
            }
        case None =>
//          val x = q.groupBy(_._1).map{x =>
//            val yy = x._2.map(_._3)
//
//            (x._1, 0.0, ByteString(yy.flatten.mkString(",")))
//          }.toList.sortBy(_._1)
//
//          context.become(queue(x))
          println("list is empty but thats ok sometimes i think")
      }
    case x: String =>

      println(s"splitting $x")
      split(new File(x))

      val filesToLoad =
        listAllFiles(temp).filter(_.getName.contains(new File(x).getName)).toList

      self ! filesToLoad

    case Get =>
      //TODO Rather than getting the next on i need the current packet of data
      //and then send it back to the sender
      //and then something to refresh the current data packet in x amount of seconds...?s

      q.headOption match {
        case Some(data: (String, Double, ByteString)) =>
          sender() ! data._3
          val newQueue = q.drop(1) ++ List(data)
          context.become(queue(newQueue))
        case None =>
          println("no more")
      }
    case x: ByteString =>
      //println("added")
      //context.become(queue(q ++ List(x)))

    case x: (String, Double, ByteString) =>
      //TODO Add the timestamp
      val ff = q.size
      println(ff)

      context.become(queue(q ++ List(x)))

  }

  override def preStart(): Unit = {
    println(self.path)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("we restarted")
  }

}
