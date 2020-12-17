package com.zeab.tvturner2

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.zeab.tvturner2.feature.channelmanager.ChannelManager
import com.zeab.tvturner2.service.{AppConf, FileHelpers, HttpServiceBase}
import net.bramp.ffmpeg.{FFmpeg, FFprobe}

object TvTurner2 extends App with HttpServiceBase with FileHelpers{

  //FFMPEG Settings
  val ffmpeg: FFmpeg = new FFmpeg(AppConf.ffmpegPath)
  val ffprobe: FFprobe = new FFprobe(AppConf.ffprobePath)

  //Actor System
  implicit val system: ActorSystem = ActorSystem("TvTuner")
  implicit val mat: Materializer = Materializer(system)

  //Channel Manager
  val channelManager = system.actorOf(Props(classOf[ChannelManager], mat))

  //Service Bindings
  val bindingFuture = Http().bindAndHandle(route, AppConf.httpServiceHost, AppConf.httpServicePort)
  system.log.info(s"Server online at http://${AppConf.httpServiceHost}:${AppConf.httpServicePort}")

}
