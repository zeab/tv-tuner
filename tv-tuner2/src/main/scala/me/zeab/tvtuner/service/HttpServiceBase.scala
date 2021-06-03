package me.zeab.tvtuner.service

import akka.actor.ActorRef
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import me.zeab.tvtuner.features.discover.Discover
import me.zeab.tvtuner.features.lineup.Lineup
import me.zeab.tvtuner.features.catalog.Catalog
import me.zeab.tvtuner.features.video.Video
import me.zeab.tvtuner.features.xmltv.XmlTv

import scala.concurrent.duration.DurationInt

trait HttpServiceBase extends Directives
  with Discover with Lineup with Video with XmlTv with Catalog{

  override implicit val timeout: Timeout = Timeout(5.second)

  val testChannel: ActorRef

  val xmlTvManager: ActorRef

  val lineupManager: ActorRef

  val catalogManager: ActorRef

  def businessLogic: Route =
    discoverJson ~ deviceXml ~
      lineupStatusJson ~ lineupJson(lineupManager) ~
      video(testChannel) ~ xmlTv(xmlTvManager) ~ catalog(catalogManager)

  def routes: Route = businessLogic

}
