//package com.zeab.tvtuner
//
//import akka.actor.Actor
//import akka.util.ByteString
//
//class Channel1 extends Actor{
//
//  def receive: Receive = queue()
//
//  def queue(q: List[ByteString] = List.empty): Receive = {
//    case x: ByteString =>
//      println("added")
//      context.become(queue(q ++ List(x)))
//    case Get =>
//      q.headOption match {
//        case Some(value) =>
//          sender() ! value
//          val newQueue = q.drop(1) ++ List(value)
//          context.become(queue(newQueue))
//        case None =>
//          println("no more")
//      }
//  }
//
//}
