package com.kolade.offers.repository

import akka.Done
import com.kolade.offers.model.Offer

import scala.concurrent.Future

trait OfferRepository {
  def create(offerId: Offer): Future[Offer]

  def get(offerId: String): Future[Option[Offer]]

  def delete(offerId: String): Future[Option[Done]]

  def update(offer: Offer): Future[Offer]

  def retrieveAll(): Future[Seq[Offer]]

  def deleteAll(): Future[Done]
}
