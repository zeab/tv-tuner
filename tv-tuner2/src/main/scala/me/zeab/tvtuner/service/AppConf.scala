package me.zeab.tvtuner.service

object AppConf {

  val catalogPath: String = "catalog.json"
  val xmlTvPath: String = "xmltv.xml"
  val lineupPath: String = "lineup.json"

  val httpServicePort: Int = 8080
  val httpServiceHost: String = "0.0.0.0"

  val ffmpegPath: String = "C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe"
  val ffprobePath: String = "C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffprobe.exe"

  val exposedAddress: String = "192.168.1.252:8080"

}
