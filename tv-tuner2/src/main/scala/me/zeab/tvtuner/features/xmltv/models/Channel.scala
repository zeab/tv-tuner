package me.zeab.tvtuner.features.xmltv.models

import scala.xml.Elem

case class Channel(
                    id: String,
                    `display-name`: String,
                    icon: String = "http://192.168.1.252:8080/images/defualt.png",
                    lang: String = "en",
                  ) {
  def toXml: Elem =
    <channel id={id}>
      <display-name lang={lang}>{`display-name`}</display-name>
      <icon src={icon}/>
    </channel>
}
