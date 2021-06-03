package me.zeab.tvtuner.service

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import io.circe.Decoder
import io.circe.parser.decode

import scala.reflect.runtime.universe._

trait Unmarshallers {

  def jsonUnmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    Unmarshaller
      .stringUnmarshaller
      .forContentTypes(`application/json`)
      .map(str =>
        decode[A](str) match {
          case Right(value) => value
          case Left(ex) => throw ex
        })

  implicit final def unmarshaller[A: Decoder](implicit typeTag: TypeTag[A]): FromEntityUnmarshaller[A] =
    Unmarshaller.firstOf(jsonUnmarshaller)

}