package com.zeab.tvturner2.feature.channel

import java.io.File

import akka.actor.Actor
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import com.zeab.tvturner2.service.FileHelpers
import com.zeab.tvturner2.feature.channel.model.Get

class TestChannel(implicit mat: Materializer) extends Actor with FileHelpers {

  def receive: Receive = queue()

  def queue(q: List[List[ByteString]] = List.empty): Receive = {
    case x: ByteString =>
      //println("added")
      context.become(queue(q ++ List(List(x))))
    case Get =>
      q.headOption match {
        case Some(data: List[ByteString]) =>
          //before we send it back... lets wait till were on the 10 second mark and then send the info
          data.foreach(d => sender() ! d)
          //println("sending")
          //sender() ! data
          val newQueue: List[List[ByteString]] = q.drop(1) ++ List(data)
          context.become(queue(newQueue))
        case None =>
          println("no more")
      }
  }

  override def preStart(): Unit = {
    val file: File = new File("C:\\Users\\pyros\\Desktop\\testpattern\\tina.test.pattern.ts")
    FileIO
      .fromPath(file.toPath)
      .runForeach{ item: ByteString => self ! item}
  }

}
