package com.kolade.offers.service

import akka.Done
import com.kolade.offers.config.AppConfig
import com.kolade.offers.model.Expired._
import com.kolade.offers.model.Offer
import com.kolade.offers.repository.OfferRepository

import scala.concurrent.{ExecutionContext, Future}

class DefaultOfferService(offerRepository: OfferRepository) extends OfferService with AppConfig {

  def createOffer(validatedOffer: Offer): Future[Offer] = {
    offerRepository.create(validatedOffer)
  }

  def getOffer(offerId: String)(implicit ec: ExecutionContext): Future[Option[Offer]] = {
    offerRepository.get(offerId).map {
      offerOpt => offerOpt.map(postProcess)
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
      offers => offers.map(postProcess)
    }
  }

  def cancelAllOffers(): Future[Done] = {
    offerRepository.deleteAll()
  }

  private def postProcess(offer: Offer): Offer = {
    val link = s"$OfferLinkPrefix/${offer.offerId}"
    val isExpired = if (offer.validity.endDate.isAfterNow) No else Yes
    offer.copy(expired = Option(isExpired), link = Option(link))
  }

}

object DefaultOfferService {
  def apply(offerRepository: OfferRepository): DefaultOfferService = new DefaultOfferService(offerRepository)
}
