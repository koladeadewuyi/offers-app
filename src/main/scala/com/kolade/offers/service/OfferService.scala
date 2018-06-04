package com.kolade.offers.service

import akka.Done
import com.kolade.offers.model.Offer

import scala.concurrent.{ExecutionContext, Future}

trait OfferService {
  def createOffer(offer: Offer): Future[Offer]

  def getOffer(offerId: String)(implicit ec: ExecutionContext): Future[Option[Offer]]

  def cancelOffer(offerId: String): Future[Option[Done]]

  def updateOffer(offer: Offer): Future[Offer]

  def retrieveAllOffers()(implicit ec: ExecutionContext): Future[Seq[Offer]]

  def cancelAllOffers(): Future[Done]
}
