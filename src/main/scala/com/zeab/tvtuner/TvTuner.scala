//package com.zeab.tvtuner
//
//import akka.actor.{ActorSystem, Props}
//import akka.http.scaladsl.Http
//import akka.stream.ActorMaterializer
//import com.zeab.tvtuner.Routes.route
//
//object TvTuner extends App {
//
//  implicit val system: ActorSystem = ActorSystem("TvTuner")
//  implicit val mat: ActorMaterializer = ActorMaterializer()
//
//  val channel1 = system.actorOf(Props[Channel1])
//
//  val multiCastServer = system.actorOf(Props[MultiCastServer], "multicast_server")
//
//  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
//
//  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//
//}
