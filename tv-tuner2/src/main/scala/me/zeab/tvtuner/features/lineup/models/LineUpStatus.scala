package me.zeab.tvtuner.features.lineup.models

case class LineUpStatus(
                         ScanInProgress: Int = 0,
                         ScanPossible: Int = 1,
                         Source: String = "Cable",
                         SourceList: List[String] = List("Cable")
                       )
