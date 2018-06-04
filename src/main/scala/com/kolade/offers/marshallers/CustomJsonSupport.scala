package com.kolade.offers.marshallers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.kolade.offers.model._
import org.joda.money.Money
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

trait CustomJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {
    private val parserISO: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()
    override def write(obj: DateTime): JsString = JsString(parserISO.print(obj))

    override def read(json: JsValue): DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _ => throw DeserializationException(s"Error de-serializing $json to DateTime. Allowed format is 2018-06-03T16:11:39+01:00")
    }
  }

  implicit object MoneyJsonFormat extends RootJsonFormat[Money] {
    override def write(obj: Money): JsString = JsString(obj.toString)

    override def read(json: JsValue): Money = json match {
      case JsString(s) => Money.parse(s)
      case _ => throw DeserializationException(s"Error de-serializing $json to Money. Allowed format GBP 23.87")
    }
  }

  implicit object EnumFormat extends RootJsonFormat[Expired.Value] {
    override def write(obj: Expired.Value): JsString = JsString(obj.toString)

    override def read(json: JsValue): Expired.Value = json match {
      case JsString(s) => Expired.withName(s)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $Expired instead of $somethingElse")
    }
  }

  implicit val validityFormat: RootJsonFormat[Validity] = jsonFormat2(Validity)

  implicit val offerFormat: RootJsonFormat[Offer] = jsonFormat6(Offer)

  implicit val validatedFieldFormat: RootJsonFormat[FieldErrorInfo] = jsonFormat2(FieldErrorInfo)

  implicit val validationErrorsFormat: RootJsonFormat[ValidationRejection] = jsonFormat1(ValidationRejection)

}
