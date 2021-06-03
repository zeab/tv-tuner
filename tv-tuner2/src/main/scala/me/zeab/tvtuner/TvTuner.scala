package me.zeab.tvtuner

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import me.zeab.tvtuner.features.catalog.CatalogManager
import me.zeab.tvtuner.features.lineup.LineupManager
import me.zeab.tvtuner.features.video.ChannelFeed
import me.zeab.tvtuner.features.xmltv.XmlTvManager
import me.zeab.tvtuner.service.AppConf.{ffmpegPath, ffprobePath, httpServiceHost, httpServicePort}
import me.zeab.tvtuner.service.HttpServiceBase
import net.bramp.ffmpeg.{FFmpeg, FFprobe}

import scala.concurrent.Future

object TvTuner extends App with HttpServiceBase {

  val ffmpeg: FFmpeg = new FFmpeg(ffmpegPath)
  val ffprobe: FFprobe = new FFprobe(ffprobePath)

  //Actor System
  implicit val system: ActorSystem = ActorSystem("TvTuner")
  implicit val mat: Materializer = Materializer(system)

  val testChannel: ActorRef =
    system.actorOf(Props(classOf[ChannelFeed], "test", mat), "TestChannel")

  val lineupManager: ActorRef =
    system.actorOf(Props(classOf[LineupManager]), "LineupManager")

  val catalogManager: ActorRef =
    system.actorOf(Props(classOf[CatalogManager]), "CatalogManager")

  val xmlTvManager: ActorRef =
    system.actorOf(Props(classOf[XmlTvManager], lineupManager, catalogManager, ffprobe), "XmlTvManager")

  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, httpServiceHost, httpServicePort)
  system.log.info(s"Server online at http://${httpServiceHost}:${httpServicePort}")

}
