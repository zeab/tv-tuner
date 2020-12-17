package com.zeab.tvturner2.feature.discover

import java.net.InetAddress

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import com.zeab.tvturner2.feature.discover.models.Device
import com.zeab.tvturner2.service.AppConf
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

trait Discover extends Directives{

  //Part of the TvTuner API were mocking
  val deviceXml: Route =
    path("device.xml") {
      get {
        val inetAddress: String = s"http://${InetAddress.getLocalHost.getHostAddress}:${AppConf.httpServicePort}"
        val device: String = Device(inetAddress).toXml
        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`), device)))
      }
    }

  //Part of the TvTuner API were mocking
  val discoverJson: Route =
    path("discover.json") {
      get {
        val inetAddress: String = s"http://${InetAddress.getLocalHost.getHostAddress}:${AppConf.httpServicePort}"
        val device: String = Device(inetAddress).asJson.noSpaces
        complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), device)))
      }
    }

}
