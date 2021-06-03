package me.zeab.tvtuner.features.lineup

import akka.actor.Actor
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import me.zeab.tvtuner.features.lineup.models.LineupItem
import me.zeab.tvtuner.service.AppConf.lineupPath
import me.zeab.tvtuner.service.FileHelpers
import me.zeab.tvtuner.shared.Get

import java.io.File
import java.nio.file.{Files, Paths}
import java.util
import scala.collection.convert.ImplicitConversions.`list asScalaBuffer`
import scala.util.{Failure, Success, Try}

class LineupManager extends Actor with FileHelpers {

  def receive: Receive = lineup()

  def lineup(lineupItems: Seq[LineupItem] = Seq.empty): Receive = {
    case lineupItem: LineupItem =>
      val updatedLineup: Seq[LineupItem] =
        (lineupItems.filterNot(_.GuideNumber == lineupItem.GuideNumber) ++ Seq(lineupItem)).sortBy(_.GuideNumber).reverse
      context.become(lineup(updatedLineup))
    case Get =>
      sender() ! lineupItems
  }

  override def preStart(): Unit = {
    Try(Files.readAllLines(Paths.get(lineupPath))) match {
      case Failure(exception: Throwable) =>
        println("no lineup.json file found ... making a new one")
        createTestLineup()
      case Success(fileLines: util.List[String]) =>
        decode[Seq[LineupItem]](fileLines.toList.mkString("")) match {
          case Left(exception: circe.Error) =>
            println("unable to decode the lineup.json so wiping and creating a new one")
            createTestLineup()
          case Right(lineupItems: Seq[LineupItem]) =>
            lineupItems.foreach((lineupItem: LineupItem) => self ! lineupItem)
        }
    }
  }

  private def createTestLineup(): Unit = {
    val file: File = new File(lineupPath)
    val lineupItems: Seq[LineupItem] =
      Seq(
        LineupItem("1", "Test1", "http://localhost:8080/video?channel=1"),
        LineupItem("2", "Test2", "http://localhost:8080/video?channel=2"),
        LineupItem("3", "Test3", "http://localhost:8080/video?channel=3")
      )
    writeFile(file, lineupItems.asJson.spaces2)
    lineupItems.foreach((lineupItem: LineupItem) => self ! lineupItem)
  }

}
