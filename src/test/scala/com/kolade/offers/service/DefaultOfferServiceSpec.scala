package com.kolade.offers.service

import akka.Done
import com.kolade.offers.TestFixture
import com.kolade.offers.model.{Expired, Offer, Validity}
import com.kolade.offers.repository.OfferRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultOfferServiceSpec extends TestFixture {

  describe("DefaultOfferService") {
    describe("createOffer") {
      it("should return a future of the created offer when given an offer") {
        val offerRepositoryMock = mock[OfferRepository]
        val offer = Offer(randomUUID, ValidDescription, price(ValidAmount), Validity(now, nextDay))
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.create _).expects(offer).returns(Future.successful(offer)).once

        val result = offerService.createOffer(offer)

        result.futureValue shouldBe offer
      }

      it("should return a failed future when the offerRepository returns the same failed future") {
        val offerRepositoryMock = mock[OfferRepository]
        val expectedExceptionMessage = "Offer creation failed"
        val offerService = new DefaultOfferService(offerRepositoryMock)
        val offer = Offer(randomUUID, ValidDescription, price(ValidAmount), Validity(now, nextDay))
        (offerRepositoryMock.create _).expects(offer).returns(Future.failed(new Exception(expectedExceptionMessage))).once

        val result = offerService.createOffer(offer)

        result.failed.futureValue should have message expectedExceptionMessage
      }
    }

    describe("getOffer") {
      it("should return a future of the offer when given an offerId that exists") {
        val offerId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        val offer = Offer(offerId, ValidDescription, price(ValidAmount), Validity(now, nextDay), Option(Expired.No), link(offerId))
        (offerRepositoryMock.get _).expects(offer.offerId).returns(Future.successful(Option(offer))).once()

        val result = offerService.getOffer(offer.offerId)

        result.futureValue shouldBe Option(offer)
      }

      it("should auto-expire an offer with an endDate before the current dateTime") {
        val offerId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        val offer = Offer(offerId, ValidDescription, price(ValidAmount), Validity(now, nextDay), Option(Expired.No), link(offerId))
        (offerRepositoryMock.get _).expects(offer.offerId).returns(Future.successful(Option(offer))).once()

        withSystemTimeSetTo(nextDay.plusMillis(1)) {
          val result = offerService.getOffer(offer.offerId)

          result.futureValue shouldBe Option(offer.copy(expired = Option(Expired.Yes)))
        }
      }

      it("should return a future of None when given an offerId that does not exists") {
        val nonExistingOfferId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.get _).expects(nonExistingOfferId).returns(Future.successful(None)).once()

        val result = offerService.getOffer(nonExistingOfferId)

        result.futureValue shouldBe None
      }

      it("should return a failed future when the offerRepository returns the same failed future") {
        val offerId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val expectedExceptionMessage = "Failed to get offer"
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.get _).expects(offerId).returns(Future.failed(new Exception(expectedExceptionMessage))).once

        val result = offerService.getOffer(offerId)

        result.failed.futureValue should have message expectedExceptionMessage
      }
    }

    describe("cancelOffer") {
      it("should return a future of Option Done when given an offerId that exists") {
        val offerId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.delete _).expects(offerId).returns(Future.successful(Option(Done))).once

        val createdOfferFuture = offerService.cancelOffer(offerId)

        createdOfferFuture.futureValue shouldBe Option(Done)
      }

      it("should return a future of None when given an offerId that does not exists") {
        val nonExistingOfferId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.delete _).expects(nonExistingOfferId).returns(Future.successful(None)).once

        val result = offerService.cancelOffer(nonExistingOfferId)

        result.futureValue shouldBe None
      }

      it("should return a failed future when the offerRepository returns the same failed future") {
        val offerId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val expectedExceptionMessage = "Failed to cancel offer"
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (offerRepositoryMock.delete _).expects(offerId).returns(Future.failed(new Exception(expectedExceptionMessage))).once

        val result = offerService.cancelOffer(offerId)

        result.failed.futureValue should have message expectedExceptionMessage
      }
    }

    describe("retrieveAllOffers") {
      it("should return a future of all offers") {
        val firstOfferId = randomUUID
        val secondOfferId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val firstOffer = Offer(firstOfferId, ValidDescription, price(ValidAmount), Validity(now, nextDay), Option(Expired.No), link(firstOfferId))
        val secondOffer = Offer(secondOfferId, ValidDescription, price(ValidAmount), Validity(now, nextDay), Option(Expired.No), link(secondOfferId))
        val expectedOffers = Seq(firstOffer, secondOffer)
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (() => offerRepositoryMock.retrieveAll()).expects().returns(Future.successful(expectedOffers)).once

        val result = offerService.retrieveAllOffers()

        result.futureValue shouldBe expectedOffers
      }

      it("should auto-expire offers having their endDate before the current dateTime") {
        val firstOfferId = randomUUID
        val secondOfferId = randomUUID
        val offerRepositoryMock = mock[OfferRepository]
        val firstOffer = Offer(firstOfferId, ValidDescription, price(ValidAmount), Validity(now, nextDay), Option(Expired.No), link(firstOfferId))
        val secondOffer = Offer(secondOfferId, ValidDescription, price(ValidAmount), Validity(now, nextThreeHours), Option(Expired.No), link(secondOfferId))
        val offerService = new DefaultOfferService(offerRepositoryMock)
        val expectedOffers = Seq(firstOffer, secondOffer)
        (() => offerRepositoryMock.retrieveAll()).expects().returns(Future.successful(expectedOffers)).once

        withSystemTimeSetTo(nextThreeHours.plusMillis(1)) {
          val result = offerService.retrieveAllOffers()

          result.futureValue shouldBe Seq(firstOffer, secondOffer.copy(expired = Option(Expired.Yes)))
        }
      }

      it("should return a future of an empty list when there are no offers") {
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (() => offerRepositoryMock.retrieveAll()).expects().returns(Future.successful(Nil)).once

        val result = offerService.retrieveAllOffers()

        result.futureValue shouldBe Nil
      }

      it("should return a failed future when the offerRepository returns the same failed future") {
        val offerRepositoryMock = mock[OfferRepository]
        val expectedExceptionMessage = "Failed to retrieve all offers"
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (() => offerRepositoryMock.retrieveAll()).expects().returns(Future.failed(new Exception(expectedExceptionMessage))).once

        val result = offerService.retrieveAllOffers()

        result.failed.futureValue should have message expectedExceptionMessage
      }
    }

    describe("cancelAllOffers") {
      it("should return a future of Done") {
        val offerRepositoryMock = mock[OfferRepository]
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (() => offerRepositoryMock.deleteAll()).expects().returns(Future.successful(Done)).once

        val result = offerService.cancelAllOffers()

        result.futureValue shouldBe Done
      }

      it("should return a failed future when the offerRepository returns the same failed future") {
        val offerRepositoryMock = mock[OfferRepository]
        val expectedExceptionMessage = "Failed to cancel all offers"
        val offerService = new DefaultOfferService(offerRepositoryMock)
        (() => offerRepositoryMock.deleteAll()).expects().returns(Future.failed(new Exception(expectedExceptionMessage))).once

        val result = offerService.cancelAllOffers()

        result.failed.futureValue should have message expectedExceptionMessage
      }
    }
  }

}
