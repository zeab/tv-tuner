package me.zeab.tvtuner.features.catalog

import akka.actor.Actor
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import me.zeab.tvtuner.TvTuner.writeFile
import me.zeab.tvtuner.features.catalog.models.CatalogItem
import me.zeab.tvtuner.service.AppConf.catalogPath
import me.zeab.tvtuner.shared.Get

import java.io.File
import java.nio.file.{Files, Paths}
import java.util
import scala.collection.convert.ImplicitConversions.`list asScalaBuffer`
import scala.util.{Failure, Success, Try}

class CatalogManager extends Actor {

  def receive: Receive = catalog(Seq.empty)

  def catalog(catalogItems: Seq[CatalogItem]): Receive = {
    case catalogItem: CatalogItem =>
      val updatedCatalog: Seq[CatalogItem] =
        (catalogItems.filterNot(_.GuideNumber == catalogItem.GuideNumber) ++ Seq(catalogItem)).sortBy(_.GuideNumber)
      context.become(catalog(updatedCatalog))
    case Get =>
      sender() ! catalogItems
    case _ => println("doing another thing")
  }

  override def preStart(): Unit = {
    Try(Files.readAllLines(Paths.get(catalogPath))) match {
      case Failure(exception: Throwable) =>
        println("no catalog.json file found ... making a new one")
        createTestCatalog()
      case Success(fileLines: util.List[String]) =>
        decode[Seq[CatalogItem]](fileLines.toList.mkString("")) match {
          case Left(exception: circe.Error) =>
            println("unable to decode the catalog.json so wiping and creating a new one")
            createTestCatalog()
          case Right(catalogItems: Seq[CatalogItem]) =>
            catalogItems.foreach((catalogItem: CatalogItem) => self ! catalogItem)
        }
    }
  }

  private def createTestCatalog(): Unit = {
    val file: File = new File(catalogPath)
    val catalogItems: Seq[CatalogItem] =
      Seq(
        CatalogItem("1", Seq("C:\\Users\\pyros\\Desktop\\filestoplay\\mega.man")),
        CatalogItem("2", Seq("C:\\Users\\pyros\\Desktop\\filestoplay\\mega.man")),
        CatalogItem("3", Seq("C:\\Users\\pyros\\Desktop\\filestoplay\\mega.man"))
      )
    writeFile(file, catalogItems.asJson.spaces2)
    catalogItems.foreach((catalogItem: CatalogItem) => self ! catalogItem)
  }
}
