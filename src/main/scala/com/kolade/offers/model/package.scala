package com.kolade.offers

import akka.http.scaladsl.server.Rejection
import org.joda.money.Money
import org.joda.time.DateTime

package object model {

  object Expired extends Enumeration {
    type Expired = Value
    val Yes, No = Value
  }

  final case class Validity(startDate: DateTime, endDate: DateTime)

  final case class Offer(offerId: String,
                         description: String,
                         price: Money,
                         validity: Validity,
                         expired: Option[Expired.Value] = None,
                         link: Option[String] = None)

  final case class FieldErrorInfo(name: String, error: String)

  final case class ValidationRejection(invalidFields: Seq[FieldErrorInfo]) extends Rejection

}
