package com.kolade.offers.repository

import akka.Done
import com.github.blemale.scaffeine.Cache
import com.kolade.offers.model.Offer
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.util.Try

class DefaultOfferRepository(dataStore: Cache[String, Offer]) extends OfferRepository with Logging {

  override def create(offer: Offer): Future[Offer] = {
    withDataStore[Offer]("create") {
      dataStore.put(offer.offerId, offer)
      Future.successful(offer)
    }
  }

  override def get(offerId: String): Future[Option[Offer]] = {
    withDataStore[Option[Offer]]("get") {
      Future.successful(dataStore.getIfPresent(offerId))
    }
  }

  override def delete(offerId: String): Future[Option[Done]] = {
    withDataStore[Option[Done]]("delete") {
      dataStore.invalidate(offerId)
      Future.successful(Option(Done))
    }
  }

  override def update(offer: Offer): Future[Offer] = {
    withDataStore[Offer]("update") {
      dataStore.put(offer.offerId, offer)
      Future.successful(offer)
    }
  }

  override def retrieveAll(): Future[Seq[Offer]] = {
    Future.successful(dataStore.asMap().values.toSeq)
  }

  override def deleteAll(): Future[Done] = {
    dataStore.invalidateAll()
    Future.successful(Done)
  }

  private def withDataStore[T](operation: String)(func: => Future[T]): Future[T] = {
    val failedFuture = Future.failed(new IllegalStateException(s"Failed to $operation offer"))
    Try {
      val result = func
      logger.debug(s"$operation successfully performed")
      result
    } recover { case ex =>
      logger.error(s"Error occurred while trying to $operation offer: ${ex.getMessage}")
      failedFuture
    } getOrElse failedFuture
  }

}
