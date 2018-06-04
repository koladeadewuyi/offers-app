package com.kolade.offers

import java.util.UUID

import org.joda.time.{DateTime, DateTimeUtils}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Assertion, FunSpec, Matchers}
import spray.json.JsString

trait TestFixture extends FunSpec with Matchers with MockFactory with ScalaFutures with TableDrivenPropertyChecks
  with CustomJsonSupport {

  val ValidCost = 10
  val CostZeroOrLess = 0
  val InvalidOfferId = "< 10 chars"
  val ValidDescription = "a description that exceeds 20 characters"
  val now: DateTime = new DateTime().withMillisOfSecond(0)
  val nextThreeHours: DateTime = now.plusHours(3)
  val nextDay: DateTime = now.plusDays(1)
  val twoDaysAgo: DateTime = now.minusDays(2)

  def randomUUID: String = UUID.randomUUID.toString

  def withSystemTimeSetTo(frozenTime: DateTime)(func: => Assertion): Unit = {
    try {
      DateTimeUtils.setCurrentMillisFixed(frozenTime.getMillis)
      func
    } finally {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  def createOfferBody(description: String, cost: Int, startDate: DateTime, endDate: DateTime): String = {
    s"""
       |{
       |  "offerId": "",
       |  "description": "$description",
       |  "price": {
       |      "cost": $cost,
       |      "currency": "£"
       |  },
       |  "validity": {
       |      "startDate": ${toJsString(startDate)},
       |      "endDate": ${toJsString(endDate)}
       |  }
       |}
       |""".stripMargin
  }

  private def toJsString(dateTime: DateTime): JsString = DateJsonFormat.write(dateTime)

}
