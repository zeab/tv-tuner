package com.zeab.tvturner2.feature.channelmanager

import akka.util.ByteString

case class MediaPiece(sourceName: String, duration: Double, data: List[ByteString] = List.empty)
