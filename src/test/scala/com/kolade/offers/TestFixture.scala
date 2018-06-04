package com.kolade.offers

import java.util.UUID

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.testkit.MarshallingTestUtils
import akka.stream.ActorMaterializer
import com.kolade.offers.config.AppConfig
import com.kolade.offers.marshallers.CustomJsonSupport
import com.kolade.offers.model.{Offer, Validity}
import org.joda.money.{CurrencyUnit, Money}
import org.joda.time.{DateTime, DateTimeUtils}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Assertion, FunSpec, Matchers}

import scala.concurrent.ExecutionContext

trait TestFixture extends FunSpec with Matchers with MockFactory with ScalaFutures with TableDrivenPropertyChecks
  with CustomJsonSupport with MarshallingTestUtils with AppConfig {

  val ValidAmount = 10
  val AmountZeroOrLess = 0
  val InvalidOfferId = "< 10 chars"
  val ValidDescription = "a description that exceeds 20 characters"
  val now: DateTime = new DateTime().withMillisOfSecond(0)
  val nextThreeHours: DateTime = now.plusHours(3)
  val nextDay: DateTime = now.plusDays(1)
  val twoDaysAgo: DateTime = now.minusDays(2)

  def randomUUID: String = UUID.randomUUID.toString

  def price(amount: Int): Money = Money.of(CurrencyUnit.GBP, amount)

  def link(offerId: String): Option[String] = Option(s"$OfferLinkPrefix/$offerId")

  def withSystemTimeSetTo(frozenTime: DateTime)(func: => Assertion): Unit = {
    try {
      DateTimeUtils.setCurrentMillisFixed(frozenTime.getMillis)
      func
    } finally {
      DateTimeUtils.setCurrentMillisSystem()
    }
  }

  def createOfferEntity(description: String, amount: Int, startDate: DateTime, endDate: DateTime)
                       (implicit ec: ExecutionContext, materializer: ActorMaterializer): HttpEntity.Strict = {

    marshal[Offer](Offer("N/A", description, price(amount), Validity(startDate, endDate)))
  }

}
