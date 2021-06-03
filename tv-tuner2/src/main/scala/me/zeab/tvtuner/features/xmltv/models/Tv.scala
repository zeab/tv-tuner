package me.zeab.tvtuner.features.xmltv.models

import scala.xml.Elem

case class Tv(generatorInfoName: String, channels: Seq[Channel], programmes: Seq[Programme]) {
  def toXml: Elem =
    <tv generator-info-name={generatorInfoName}>
      {channels.flatMap(_.toXml)}
      {programmes.flatMap(_.toXml)}
    </tv>
}
