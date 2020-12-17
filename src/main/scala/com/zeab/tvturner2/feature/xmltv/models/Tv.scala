package com.zeab.tvturner2.feature.xmltv.models

import scala.xml.Elem

case class Tv(generatorInfoName: String, channels: List[Channel], programmes: List[Programme]) {
  def toXml: Elem =
    <tv generator-info-name="my-tuner">
      {channels.map(_.toXml)}
      {programmes.map(_.toXml)}
    </tv>
}
