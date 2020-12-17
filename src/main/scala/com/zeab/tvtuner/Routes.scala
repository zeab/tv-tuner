//package com.zeab.tvtuner
//
//import java.io.File
//import java.net.InetAddress
//
//import akka.NotUsed
//import akka.http.scaladsl.model.MediaType.WithFixedCharset
//import akka.http.scaladsl.model._
//import akka.http.scaladsl.server.{Directives, Route}
//import akka.stream.scaladsl.{FileIO, Source}
//import akka.stream.{IOResult, ThrottleMode}
//import akka.util.{ByteString, Timeout}
//import com.zeab.tvtuner.models.{Device, LineUpStatus, PostLineup}
//import io.circe.generic.auto._
//import io.circe.syntax._
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//import io.circe.generic.auto._
//
//object Routes extends Directives {
//
//  val utf8: HttpCharset = HttpCharsets.`UTF-8`
//  val `video/mp2t`: WithFixedCharset =
//    MediaType.customWithFixedCharset("video", "mp2t", utf8)
//  val framesPerSecond: Int = 30
//
//  private val deviceXml: Route =
//    path("device.xml") {
//      get {
//        println("device.xml")
//        val inetAddress = "http://" + InetAddress.getLocalHost.getHostAddress + ":8080"
//        val device: String = Device(inetAddress, inetAddress).toXml
//        val x = HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), device))
//        complete(x)
//      }
//    }
//
//  private val discoverJson: Route =
//    path("discover.json") {
//      get {
//        println("discover")
//        val inetAddress = "http://" + InetAddress.getLocalHost.getHostAddress + ":8080"
//        val device: String = Device(inetAddress, inetAddress).asJson.noSpaces
//        val x = HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), device))
//        complete(x)
//      }
//    }
//
//  private val lineupStatusJson: Route =
//    path("lineup_status.json") {
//      get {
//        println("status")
//        val lineupStatus: String = LineUpStatus().asJson.noSpaces
//        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), lineupStatus)))
//      }
//    }
//
//  private val lineupJson: Route =
//    path("lineup.json") {
//      get {
//        //need to change this to read from the file instead
//        println("lineup")
//        val y = "[{\"GuideNumber\":\"1\",\"GuideName\":\"Channel 1\",\"URL\":\"http://192.168.1.144:8080/video?channel=1\"}]"
//        //val y = "[{\"GuideNumber\":\"1\",\"GuideName\":\"Bob's Diner\",\"URL\":\"http://192.168.1.252:8080/video?channel=1\"}]"
//        val x = HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), y))
//        complete(x)
//      } ~
//        post{
//          decodeRequest {
//            entity(as[String]) { dd =>
//
//              //should add the line up to a database of some kind? or just well a text file really?
//
//              complete("")
//            }
//          }
//        }
//    }
//
//  private val video: Route =
//    path("video") {
//      get {
//        println("video")
//        implicit val timeout: Timeout = Timeout(5.second)
//        val channelSource: Source[ByteString, NotUsed] =
//          Source.repeat(Get).ask[ByteString](parallelism = 1)(TvTuner.channel1)
//
//        complete(
//            HttpResponse(
//              StatusCodes.OK,
//              entity = HttpEntity(
//                `video/mp2t`,
//                channelSource
////                  .throttle(
////                  framesPerSecond,
////                  1.second,
////                  framesPerSecond * 30, // maximumBurst
////                  ThrottleMode.Shaping)
//              )
//            )
//          )
//      }
//    }
//
//  private val setup: Route =
//    extractMaterializer { implicit mat =>
//      path("setup") {
//        get {
//          println("setup")
//
//          //get the basic set stream logo and stream it over and over and over and over
//
//          //where does this initial stream come from??
//
//          //val file = new File("C:\\Users\\pyros\\Desktop\\out\\s01e12.ts")
//          val file = new File("C:\\Users\\pyros\\Desktop\\out\\channel1.ts")
//          val bb =
//            FileIO.fromPath(file.toPath, 4096).runForeach(item => TvTuner.channel1 ! item)
//          complete("")
//        }
//      }
//    }
//
//  val route: Route = deviceXml ~ discoverJson ~ lineupStatusJson ~ lineupJson ~ video ~ setup
//
//}
