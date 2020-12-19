package com.zeab.tvturner2.feature.channelmanager

import akka.Done
import akka.actor.Actor
import akka.util.ByteString

class FileOpener(name: String) extends Actor{

  def receive: Receive = queue()

  def queue(q: List[ByteString] = List.empty): Receive = {
    case incomingData: ByteString =>
      context.become(queue(q ++ List(incomingData)))
    case _: Done =>
      sender() ! MediaPiece(name, 0.0, q)
      context.stop(self)
  }

}
