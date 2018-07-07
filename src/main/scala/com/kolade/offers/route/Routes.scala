package com.kolade.offers.route

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import com.kolade.offers.marshallers.CustomJsonSupport
import com.kolade.offers.model.{Offer, ValidationRejection}
import com.kolade.offers.service.OfferService
import com.kolade.offers.validation.OfferValidator
import com.kolade.offers.validation.ValidationDirective._
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

class Routes(offerService: OfferService) extends CustomJsonSupport with Logging {

  implicit val offerValidator: OfferValidator.type = OfferValidator

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case NonFatal(ex) =>
        extractUri { uri =>
          logger.info(s"Request to $uri could not be handled normally: ${ex.getMessage}")
          complete(HttpResponse(InternalServerError, entity = ex.getMessage))
        }
    }

  implicit def rejectionHandler: RejectionHandler = {
    RejectionHandler.newBuilder()
      .handle { case ex: ValidationRejection =>
        val validationErrors = HttpEntity(ContentTypes.`application/json`, validationErrorsFormat.write(ex).toString)
        complete(HttpResponse(StatusCodes.BadRequest, entity = validationErrors))
      }.result().withFallback(RejectionHandler.default)
  }

  val route: Route = {

    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        path("offers") {
          get {
            complete(offerService.retrieveAllOffers())
          } ~
          delete {
            onSuccess(offerService.cancelAllOffers()) { _ =>
              complete("all offers deleted")
            }
          } ~
          post {
            entity(as[Offer]) { offer =>
              val generatedOfferId = UUID.randomUUID.toString
              validateModel(offer.copy(offerId = generatedOfferId)).apply { validatedOffer =>
                complete(StatusCodes.Created, offerService.createOffer(validatedOffer))
              }
            }
          }
        } ~
        pathPrefix("offers" / Segment) { offerId =>
          get {
            onSuccess(offerService.getOffer(offerId)) {
              case Some(offer) => complete(offer)
              case None => complete(NotFound)
            }
          } ~
          delete {
            onSuccess(offerService.cancelOffer(offerId)) {
              case Some(_) => complete(s"offer $offerId deleted")
              case _ => complete(NotFound)
            }
          } ~
          put {
            entity(as[Offer]) { offer =>
              validateModel(offer.copy(offerId = offerId)).apply { validatedOffer =>
                complete(StatusCodes.Created, offerService.updateOffer(validatedOffer))
              }
            }
          }
        }
      }
    }

  }

}

object Routes {
  def apply(offerService: OfferService): Routes = new Routes(offerService)
}
