package com.kolade.offers.route

import akka.Done
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kolade.offers.TestFixture
import com.kolade.offers.model._
import com.kolade.offers.service.DefaultOfferService

import scala.concurrent.ExecutionContext._
import scala.concurrent._
import scala.language.implicitConversions

class RoutesSpec extends TestFixture with ScalatestRouteTest {

  private val offerServiceMock = mock[DefaultOfferService]
  private val route = Routes(offerServiceMock).route

  describe("Routes") {
    describe("GET") {
      it("should return an offer when a request is made to path /offers/<offerId>") {
        val offerId = randomUUID
        val expectedOffer = Offer(offerId, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        (offerServiceMock.getOffer(_: String)(_: ExecutionContext)).expects(offerId, global).returns(Future(Option(expectedOffer))).once

        Get(s"/offers/$offerId") ~> route ~> check {
          status shouldBe OK
          responseAs[Offer] shouldEqual expectedOffer
        }
      }

      it("should return all offers when a request is made to path /offers") {
        val firstOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val secondOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val expectedOffers = Seq(firstOffer, secondOffer)
        (offerServiceMock.retrieveAllOffers()(_: ExecutionContext)).expects(global).returns(Future(expectedOffers)).once

        Get("/offers") ~> route ~> check {
          status shouldBe OK
          responseAs[Seq[Offer]] should contain theSameElementsAs expectedOffers
        }
      }

      it("should return 404 error when a request is made to path /offers/<offerId> but no offer could be found") {
        val offerId = randomUUID
        (offerServiceMock.getOffer(_: String)(_: ExecutionContext)).expects(offerId, global).returns(Future(None)).once

        Get(s"/offers/$offerId") ~> route ~> check {
          status shouldBe NotFound
          responseAs[String] shouldBe "The requested resource could not be found but may be available again in the future."
        }
      }

      it("should return 500 error with custom message when a request is made to path /offers/<offerId> but the offerService returns a failed future") {
        val offerId = randomUUID
        val exceptionMessage = "Failed to get offer."
        (offerServiceMock.getOffer(_: String)(_: ExecutionContext)).expects(offerId, global).returns(Future.failed(new Exception(exceptionMessage))).once

        Get(s"/offers/$offerId") ~> route ~> check {
          status shouldBe InternalServerError
          responseAs[String] shouldBe exceptionMessage
        }
      }
    }

    describe("POST") {
      it("should return the created offer when a request is made to path /offers") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        (offerServiceMock.createOffer _).expects(*).returns(Future(offer)).once
        val validOfferBody = createOfferBody(ValidDescription, ValidCost, now, nextDay)

        Post("/offers", HttpEntity(ContentTypes.`application/json`, validOfferBody)) ~> route ~> check {
          status shouldBe Created
          responseAs[Offer] shouldBe offer
        }
      }

      it("should return validation errors info when a request is made to path /offers with invalid fields") {
        val inValidOfferBody = createOfferBody(ValidDescription, CostZeroOrLess, now, nextDay)

        Post("/offers", HttpEntity(ContentTypes.`application/json`, inValidOfferBody)) ~> route ~> check {
          status shouldBe BadRequest
          responseAs[ValidationRejection] shouldBe ValidationRejection(Seq(FieldErrorInfo("cost", "offer cost must be greater than 0")))
        }
      }
    }

    describe("PUT") {
      it("should return the updated offer when a request is made to path /offers/<offerId>") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        (offerServiceMock.updateOffer _).expects(*).returns(Future(offer)).once
        val validOfferBody = createOfferBody(ValidDescription, ValidCost, now, nextDay)

        Put(s"/offers/${offer.offerId}", HttpEntity(ContentTypes.`application/json`, validOfferBody)) ~> route ~> check {
          status shouldBe Created
          responseAs[Offer] shouldBe offer
        }
      }

      it("should return validation errors info when a request is made to path /offers/<offerId> with invalid fields") {
        val offerId = randomUUID
        val inValidOfferBody = createOfferBody(ValidDescription, CostZeroOrLess, now, nextDay)

        Put(s"/offers/$offerId", HttpEntity(ContentTypes.`application/json`, inValidOfferBody)) ~> route ~> check {
          status shouldBe BadRequest
          responseAs[ValidationRejection] shouldBe ValidationRejection(Seq(FieldErrorInfo("cost", "offer cost must be greater than 0")))
        }
      }
    }

    describe("DELETE") {
      it("should complete successfully when a request to delete all offers is made to path /offers") {
        (() => offerServiceMock.cancelAllOffers()).expects().returns(Future(Done)).once

        Delete("/offers") ~> route ~> check {
          status shouldBe OK
          responseAs[String] shouldBe empty
        }
      }

      it("should complete successfully when a request to delete an offer is made to path /offers/<offerId>") {
        val offerId = randomUUID
        (offerServiceMock.cancelOffer _).expects(offerId).returns(Future(Option(Done))).once

        Delete(s"/offers/$offerId") ~> route ~> check {
          status shouldBe OK
          responseAs[String] shouldBe empty
        }
      }
    }

    describe("nonExistentPaths") {
      val nonExistentPathScenarios = Table(
        "requestMethod",
        Get,
        Post,
        Put,
        Delete,
        Patch
      )

      forAll(nonExistentPathScenarios) { requestMethod =>
        it(s"should return 404 error for ${requestMethod.method.value} requests to non existent path") {
          requestMethod("/non-existent-path") ~> route ~> check {
            status shouldBe NotFound
            responseAs[String] shouldBe "The requested resource could not be found."
          }
        }
      }
    }
  }

}
