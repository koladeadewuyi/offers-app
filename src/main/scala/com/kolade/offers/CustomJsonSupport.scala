package com.kolade.offers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.kolade.offers.model._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

trait CustomJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {
    private val parserISO: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()

    override def write(obj: DateTime) = JsString(parserISO.print(obj))

    override def read(json: JsValue): DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _ => throw DeserializationException(s"Error de-serializing $json to DateTime")
    }
  }

  implicit object EnumFormat extends RootJsonFormat[Expired.Value] {
    override def write(obj: Expired.Value): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Expired.Value = json match {
      case JsString(s) => Expired.withName(s)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $Expired instead of $somethingElse")
    }
  }

  implicit val validityFormat: RootJsonFormat[Validity] = jsonFormat2(Validity)

  implicit val priceFormat: RootJsonFormat[Price] = jsonFormat2(Price)

  implicit val offerFormat: RootJsonFormat[Offer] = jsonFormat5(Offer)

  implicit val validatedFieldFormat: RootJsonFormat[FieldErrorInfo] = jsonFormat2(FieldErrorInfo)

  implicit val validationErrorsFormat: RootJsonFormat[ValidationRejection] = jsonFormat1(ValidationRejection)

}
