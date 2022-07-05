package api.gen

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypeRange, ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}

import scala.concurrent.Future

trait Implicits {

  private def jsonContentTypes: List[ContentTypeRange] = List(ContentTypes.`application/json`)

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap(_ => _ => json => decode[A](json).fold(Future.failed, Future.successful))

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`) { a =>
      HttpEntity(ContentTypes.`application/json`, a.asJson.noSpaces)
    }

}
