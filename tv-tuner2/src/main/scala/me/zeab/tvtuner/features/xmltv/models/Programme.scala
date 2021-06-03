package me.zeab.tvtuner.features.xmltv.models

import scala.xml.Elem

case class Programme(
                      start: String,
                      stop: String,
                      channel: String,
                      title: String,
                      lang: String,
                      subTitle: String,
                      desc: String,
                      icon: String = "http://192.168.1.252:8080/images/defualt.png",
                      rating: String = "PG",
                      episodeNum: String = "0 . 2 . 0/1"
                    ) {
  rating

  def toXml: Elem =
    <programme start={start} stop={stop} channel={channel}>
      <title lang="en">
        {title}
      </title>
      <previously-shown/>
      <sub-title lang="en">
        {subTitle}
      </sub-title>
      <episode-num system="xmltv_ns">
        {episodeNum}
      </episode-num>
      <icon src={icon}/>
      <desc lang="en">
        {desc}
      </desc>
      <rating system="MPAA">
        <value>
          {rating}
        </value>
      </rating>
    </programme>

}
