package com.kolade.offers.service

import akka.Done
import com.kolade.offers.model.Expired._
import com.kolade.offers.model.Offer
import com.kolade.offers.repository.OfferRepository

import scala.concurrent.{ExecutionContext, Future}

class DefaultOfferService(offerRepository: OfferRepository) extends OfferService {

  def createOffer(validatedOffer: Offer): Future[Offer] = {
    offerRepository.create(validatedOffer)
  }

  def getOffer(offerId: String)(implicit ec: ExecutionContext): Future[Option[Offer]] = {
    offerRepository.get(offerId).map {
      offerOpt => offerOpt.map(autoExpire)
    }
  }

  def cancelOffer(offerId: String): Future[Option[Done]] = {
    offerRepository.delete(offerId)
  }

  def updateOffer(validatedOffer: Offer): Future[Offer] = {
    offerRepository.update(validatedOffer)
  }

  def retrieveAllOffers()(implicit ec: ExecutionContext): Future[Seq[Offer]] = {
    offerRepository.retrieveAll().map {
      offers => offers.map(autoExpire)
    }
  }

  def cancelAllOffers(): Future[Done] = {
    offerRepository.deleteAll()
  }

  private def autoExpire(offer: Offer): Offer = {
    val isExpired = if (offer.validity.endDate.isAfterNow) No else Yes
    offer.copy(expired = Option(isExpired))
  }

}

object DefaultOfferService {
  def apply(offerRepository: OfferRepository) = new DefaultOfferService(offerRepository)
}
