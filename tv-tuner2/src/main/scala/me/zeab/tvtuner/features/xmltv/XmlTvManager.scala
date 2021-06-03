package me.zeab.tvtuner.features.xmltv

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import me.zeab.tvtuner.features.catalog.models.CatalogItem
import me.zeab.tvtuner.features.lineup.models.LineupItem
import me.zeab.tvtuner.features.xmltv.models.{Channel, Programme, Tv}
import me.zeab.tvtuner.service.AppConf.xmlTvPath
import me.zeab.tvtuner.service.{DateTimeFormatters, FileHelpers}
import me.zeab.tvtuner.shared.{Get, Update}
import net.bramp.ffmpeg.FFprobe

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.ZonedDateTime
import java.util
import scala.collection.convert.ImplicitConversions.`list asScalaBuffer`
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, Node, PrettyPrinter}

class XmlTvManager(lineupManager: ActorRef, catalogManager: ActorRef, ffprobe: FFprobe) extends Actor with FileHelpers with DateTimeFormatters {

  val totalSecondsToFill: Int = 3600

  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val timeout: Timeout = Timeout(5.second)

  def receive: Receive = xmlTv()

  def xmlTv(xmlTvItems: Elem = <empty/>): Receive = {
    case Get =>
      sender() ! xmlTvItems
    case xmlTvItems: Elem =>
      context.become(xmlTv(xmlTvItems))
    case Update =>
      (lineupManager ? Get).mapTo[Seq[LineupItem]].flatMap { lineupItems: Seq[LineupItem] =>
        (catalogManager ? Get).mapTo[Seq[CatalogItem]].map { catalogItems: Seq[CatalogItem] =>
          updateXmlTv(xmlTvItems, lineupItems, catalogItems)
        }
      }.pipeTo(self)
        .onComplete { _ =>
          context.system.scheduler.scheduleOnce(10.second)(self ! Update)
        }
    case tv: Tv =>
      val xml: Elem = tv.toXml
      val prettyPrinter: PrettyPrinter = new scala.xml.PrettyPrinter(200, 2)
      val prettyXml: String = prettyPrinter.format(xml)
      if (prettyXml == prettyPrinter.format(xmlTvItems)) println("the xml is the same so were doing nothing")
      else {
        val file: File = new File(xmlTvPath)
        println("xml is not the same updating the file with the new info")
        writeFile(file, prettyXml)
        context.become(xmlTv(xml))
      }
  }

  def updateXmlTv(xmlTv: Elem, lineupItems: Seq[LineupItem], catalogItems: Seq[CatalogItem]): Tv = {

    val now: ZonedDateTime = ZonedDateTime.now()

    val channels: Seq[Channel] =
      lineupItems.map { lineupItem: LineupItem =>
        Channel(lineupItem.GuideNumber, lineupItem.GuideName)
      }

    val programmes: Seq[Programme] = updateProgramme(now, catalogItems, xmlTv.flatMap { node: Node =>
      (node \\ "programme").map { programmeNode: Node =>
        Programme(
          programmeNode.attribute("start").map(_.text).mkString,
          programmeNode.attribute("stop").map(_.text).mkString,
          programmeNode.attribute("channel").map(_.text).mkString,
          (programmeNode \ "title").text,
          "en",
          (programmeNode \ "sub-title").text,
          (programmeNode \ "desc").text,
          (programmeNode \ "icon").text
        )
      }
    })

    Tv("tv-tuner", channels, programmes)
  }

  def updateProgramme(now: ZonedDateTime, catalogItems: Seq[CatalogItem], oldProgrammes: Seq[Programme]): Seq[Programme] = {
    if (oldProgrammes.isEmpty) {
      println("there are no programmes")
      catalogItems.flatMap{catalogItem: CatalogItem =>
        val allPossibleFiles = catalogItem.dir.flatMap(ff => listAllFiles(ff))
        val r = scala.util.Random
        val mediaProbe = ffprobe.probe(allPossibleFiles(r.nextInt(allPossibleFiles.size)).getPath)

        val startTimeX: String = now.format(standardDateTimeFormat)
        val endTime: String = now.plusSeconds(mediaProbe.format.duration.toLong).format(standardDateTimeFormat)

        val programme = Seq(Programme(startTimeX, endTime, catalogItem.GuideNumber, s"${mediaProbe.format.filename}", "en", "sub-test", "test description"))
        updateProgramme(now, catalogItems, oldProgrammes ++ programme)
      }
    }
    else
      oldProgrammes.filter { programme: Programme =>
        ZonedDateTime.parse(programme.stop, standardDateTimeFormat).isAfter(now)
      }.groupBy(_.channel).flatMap {
        case (channel: String, programmes: Seq[Programme]) =>
          //ok for this one do I actually need to add any new items
          programmes.sortBy(_.stop).reverse.headOption match {
            case Some(programme: Programme) =>
              val stopTime = ZonedDateTime.parse(programme.stop, standardDateTimeFormat)
              val secondsLeft = stopTime.toEpochSecond - now.toEpochSecond
              if (secondsLeft <= totalSecondsToFill) {
                println(s"seconds less that $totalSecondsToFill")
                catalogItems.find(_.GuideNumber == channel) match {
                  case Some(catalogItem: CatalogItem) =>
                    val allPossibleFiles = catalogItem.dir.flatMap(ff => listAllFiles(ff))
                    val r = scala.util.Random
                    val mediaProbe = ffprobe.probe(allPossibleFiles(r.nextInt(allPossibleFiles.size)).getPath)

                    val startTimeX: String = stopTime.format(standardDateTimeFormat)
                    val endTime: String = stopTime.plusSeconds(mediaProbe.format.duration.toLong).format(standardDateTimeFormat)

                    val programme = Seq(Programme(startTimeX, endTime, catalogItem.GuideNumber, s"${mediaProbe.format.filename}", "en", "sub-test", "test description"))
                    updateProgramme(now, catalogItems, programmes ++ programme)
                  case None =>
                    println("we cant find a catalog for a line up item")
                    Seq.empty
                }
              }
              else {
                println("i think we have enough programmes on the list so just returning that")
                programmes
              }
            case None =>
              println("we got a none in xml tv manager but i dont think we should have...")
              Seq.empty
          }
      }.toSeq
  }

  override def preStart(): Unit = {
    Try(Files.readAllLines(Paths.get(xmlTvPath))) match {
      case Failure(exception: Throwable) =>
        println("no xmltv.xml file found ... making a new one")
        self ! Tv("tv-tuner", Seq.empty, Seq.empty).toXml
      case Success(fileLines: util.List[String]) =>
        println("found an xmltv.xml file so using that")
        self ! scala.xml.XML.loadString(fileLines.toList.mkString("\n"))
    }

    //Start the updater
    context.system.scheduler.scheduleOnce(10.second)(self ! Update)
  }

}
