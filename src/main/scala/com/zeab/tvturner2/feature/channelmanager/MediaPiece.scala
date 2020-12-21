package com.zeab.tvturner2.feature.channelmanager

import java.time.ZonedDateTime

import akka.util.ByteString

case class MediaPiece(sourceName: String, duration: Double, startDateTime: ZonedDateTime, endDateTime: ZonedDateTime, data: List[ByteString] = List.empty)
