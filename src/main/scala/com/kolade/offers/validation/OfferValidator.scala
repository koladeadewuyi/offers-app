package com.kolade.offers.validation

import com.kolade.offers.config.AppConfig
import com.kolade.offers.model.{FieldErrorInfo, Offer}
import org.joda.time.DateTime

object OfferValidator extends Validator[Offer] with AppConfig {

  override def apply(offer: Offer): Seq[FieldErrorInfo] = {

    val offerIdErrorOpt = validation(
      offerIdRule(offer.offerId),
      "offerId",
      s"offerId must exceed $MinAllowedOfferIdLength characters")

    val descriptionErrorOpt = validation(
      descriptionRule(offer.description),
      "description",
      s"offer description must exceed $MinAllowedDescriptionLength characters")

    val costErrorOpt = validation(
      costRule(offer.price.cost),
      "cost",
      s"offer cost must be greater than $MinAllowedCost")

    val startDateErrorOpt = validation(startDateRule(
      offer.validity.startDate),
      "startDate",
      s"offer start date must not be earlier than $MaxDaysAgoAllowedForOfferStartDate day(s) ago")

    val endDateErrorOpt = validation(endDateRule(
      offer.validity.startDate, offer.validity.endDate),
      "endDate",
      "offer end date must be after start date")

    (offerIdErrorOpt :: descriptionErrorOpt :: costErrorOpt :: startDateErrorOpt :: endDateErrorOpt :: Nil).flatten
  }

  private def offerIdRule(offerId: String) = offerId.length > MinAllowedOfferIdLength

  private def descriptionRule(description: String) = description.length > MinAllowedDescriptionLength

  private def costRule(cost: Int) = cost > MinAllowedCost

  private def startDateRule(startDate: DateTime) = startDate.isAfter(new DateTime().minusDays(MaxDaysAgoAllowedForOfferStartDate))

  private def endDateRule(startDate: DateTime, endDate: DateTime) = endDate.isAfter(startDate)

}
