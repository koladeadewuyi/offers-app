package com.kolade.offers.repository

import akka.Done
import com.github.benmanes.caffeine.cache.{CacheWriter, RemovalCause}
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.kolade.offers.TestFixture
import com.kolade.offers.model.{Expired, Offer, Price, Validity}

class DefaultOfferRepositorySpec extends TestFixture {

  val OfferStoreCacheSize = 100

  describe("DefaultOfferRepository") {
    describe("create") {
      it("should persist a given offer") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = createDataStore()

        val result = new DefaultOfferRepository(dataStoreMock).create(offer)

        result.futureValue shouldBe offer
        dataStoreMock.getIfPresent(offer.offerId) shouldBe Some(offer)
      }

      it("should return a failed future with appropriate message when the dataStore throws an exception") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = failingDataStore

        val result = new DefaultOfferRepository(dataStoreMock).create(offer)

        val futureValue = result.failed.futureValue
        futureValue shouldBe an[Exception]
        futureValue.getMessage shouldBe "Failed to create offer"
      }
    }

    describe("get") {
      it("should retrieve an existing offer when given the offerId") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay), Some(Expired.No))
        val dataStoreMock = createDataStore()
        dataStoreMock.put(offer.offerId, offer)

        val result = new DefaultOfferRepository(dataStoreMock).get(offer.offerId)

        result.futureValue shouldBe Some(offer)
      }

      it("should return a future of None when offerId does not exist") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = createDataStore()

        val result = new DefaultOfferRepository(dataStoreMock).get(offer.offerId)

        result.futureValue shouldBe None
      }

      it("should return a failed future with appropriate message when the dataStore throws an exception") {
        val exceptionThrowingOfferId = null

        val result = new DefaultOfferRepository(failingDataStore).get(exceptionThrowingOfferId)

        val futureValue = result.failed.futureValue
        futureValue shouldBe a[Exception]
        futureValue.getMessage shouldBe "Failed to get offer"
      }
    }

    describe("delete") {
      it("should delete an existing offer when given the offerId") {
        val offer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = createDataStore()
        dataStoreMock.put(offer.offerId, offer)

        val result = new DefaultOfferRepository(dataStoreMock).delete(offer.offerId)

        result.futureValue shouldBe Some(Done)
        dataStoreMock.getIfPresent(offer.offerId) shouldBe None
      }

      it("should return a failed future with appropriate message when the dataStore throws an exception") {
        val exceptionThrowingOfferId = null

        val result = new DefaultOfferRepository(failingDataStore).delete(exceptionThrowingOfferId)

        val futureValue = result.failed.futureValue
        futureValue shouldBe a[Exception]
        futureValue.getMessage shouldBe "Failed to delete offer"
      }
    }

    describe("update") {
      it("should update an existing offer when given an update") {
        val originalOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val updatedOffer = Offer(originalOffer.offerId, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = createDataStore()
        dataStoreMock.put(originalOffer.offerId, originalOffer)

        val result = new DefaultOfferRepository(dataStoreMock).update(updatedOffer)

        result.futureValue shouldBe updatedOffer
        dataStoreMock.getIfPresent(originalOffer.offerId) shouldBe Some(updatedOffer)
      }

      it("should return a failed future with appropriate message when the dataStore throws an exception") {
        val updatedOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))

        val result = new DefaultOfferRepository(failingDataStore).update(updatedOffer)

        val futureValue = result.failed.futureValue
        futureValue shouldBe a[Exception]
        futureValue.getMessage shouldBe "Failed to update offer"
      }
    }

    describe("deleteAll") {
      it("should delete all offers") {
        val firstOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val secondOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay))
        val dataStoreMock = createDataStore()
        dataStoreMock.put(firstOffer.offerId, firstOffer)
        dataStoreMock.put(secondOffer.offerId, secondOffer)

        val result = new DefaultOfferRepository(dataStoreMock).deleteAll()

        result.futureValue shouldBe Done
        dataStoreMock.asMap().keys shouldBe empty
      }
    }

    describe("retrieveAll") {
      it("should retrieve all offers") {
        val firstOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay), Some(Expired.No))
        val secondOffer = Offer(randomUUID, ValidDescription, Price(ValidCost), Validity(now, nextDay), Some(Expired.No))
        val dataStoreMock = createDataStore()
        dataStoreMock.put(firstOffer.offerId, firstOffer)
        dataStoreMock.put(secondOffer.offerId, secondOffer)

        val result = new DefaultOfferRepository(dataStoreMock).retrieveAll()

        result.futureValue should contain theSameElementsAs Seq(firstOffer, secondOffer)
      }

      it("should return an empty list when dataStore is empty") {
        val dataStoreMock = createDataStore()

        val result = new DefaultOfferRepository(dataStoreMock).retrieveAll()

        result.futureValue shouldBe Nil
      }
    }

  }

  private def createDataStore(): Cache[String, Offer] = {
    Scaffeine()
      .maximumSize(OfferStoreCacheSize)
      .build[String, Offer]()
  }

  private def failingDataStore: Cache[String, Offer] = {
    val failingCacheWriter = new CacheWriter[String, Offer] {
      override def delete(key: String, value: Offer, cause: RemovalCause): Unit = throw new Exception("delete failed")

      override def write(key: String, value: Offer): Unit = throw new Exception("write failed")
    }

    Scaffeine()
      .maximumSize(OfferStoreCacheSize)
      .writer(failingCacheWriter)
      .build[String, Offer]()
  }

}
