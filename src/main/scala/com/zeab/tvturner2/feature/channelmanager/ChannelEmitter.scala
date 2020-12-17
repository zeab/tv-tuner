package com.zeab.tvturner2.feature.channelmanager

import java.io.File

import akka.actor.{Actor, ActorRef}
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import com.zeab.tvturner2.feature.channel.model.Get
import com.zeab.tvturner2.service.FileHelpers

import scala.concurrent.ExecutionContext

class ChannelEmitter(implicit mat: Materializer) extends Actor with FileHelpers {

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = queue()

  def queue(q: List[ByteString] = List.empty): Receive = {
    case x: String =>

      val temp = new File("temp")
      temp.mkdir()

      val filesToLoad =
        listAllFiles(temp).filter(_.getName.contains(x))
      filesToLoad.foreach{ file =>
        FileIO
          .fromPath(file.toPath, 4096)
          .runForeach { item: ByteString => self ! item }
      }
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
    case x: ActorRef =>
    //add this actor the the actors that need to be told about the data
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
