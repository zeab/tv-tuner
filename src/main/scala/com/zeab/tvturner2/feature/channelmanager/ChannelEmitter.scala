package com.zeab.tvturner2.feature.channelmanager

import java.io.File

import akka.actor.{Actor, ActorRef}
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import com.zeab.tvturner2.feature.channel.model.Get
import com.zeab.tvturner2.service.{AppConf, FFmpegStuff, FileHelpers}

import scala.concurrent.ExecutionContext

class ChannelEmitter(implicit mat: Materializer) extends Actor with FileHelpers with FFmpegStuff {

  implicit val ec: ExecutionContext = context.system.dispatcher

  val temp = new File(AppConf.tempPath)

  def receive: Receive = queue()

  def queue(q: List[ByteString] = List.empty): Receive = {
    case x: List[File] =>
      x.headOption match {
        case Some(file: File) =>
          FileIO
            .fromPath(file.toPath, 4096)
            .runForeach { item: ByteString => self ! item }
            .onComplete{_ =>
              self ! x.drop(1)
              file.delete()
            }
        case None => println("list is empty but thats ok sometimes i think")
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
        case Some(data: ByteString) =>
          sender() ! data
          val newQueue: List[ByteString] = q.drop(1) ++ List(data)
          context.become(queue(newQueue))
        case None =>
          println("no more")
      }
    case x: ByteString =>
      //println("added")
      context.become(queue(q ++ List(x)))
  }

  override def preStart(): Unit = {
    println(self.path)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("we restarted")
  }

}
