package com.zeab.tvturner2.service

import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.channel.Channel
import com.zeab.tvturner2.feature.discover.Discover
import com.zeab.tvturner2.feature.lineup.Lineup
import com.zeab.tvturner2.feature.video.Video
import com.zeab.tvturner2.feature.xmltv.XmlTv

trait HttpServiceBase extends Directives
  with Discover with Lineup with Video
  with XmlTv with Channel {

  def businessLogic: Route =
    discoverJson ~ deviceXml ~
      lineupStatusJson ~ lineupJson ~
      video ~ xmlTv ~ channel

  def route: Route = businessLogic

}
