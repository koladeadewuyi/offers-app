package com.kolade.offers

import java.net.ServerSocket

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.kolade.offers.config.AppConfig
import com.kolade.offers.model._
import com.kolade.offers.repository.DefaultOfferRepository
import com.kolade.offers.service.DefaultOfferService
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{Assertion, GivenWhenThen, TryValues}

import scala.concurrent.ExecutionContext.Implicits.global

class ServerSpec extends TestFixture with TryValues with AppConfig with GivenWhenThen {

  override val ApiPort: Int = randomAvailablePort()
  implicit val actorSystem: ActorSystem = ActorSystem("ServerSpec")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(10, Seconds), Span(100, Millis))

  describe("Server") {
    it("should allow creation of an offer") {
      val entity = createOfferEntity(ValidDescription, ValidCost, now, nextDay)

      Given("the server is running")
      withRunningServer {

        When("a successful request to create an offer is made")
        whenReady(makeRequest("/offers", POST, entity)) { response =>
          response.status shouldBe Created
          val offerId = unmarshal[Offer](response.entity).success.value.offerId

          Then("the offer should be retrievable using its offerId")
          whenReady(makeRequest(s"/offers/$offerId")) { response =>
            response.status shouldBe OK
            val expectedOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(now, nextDay), Option(Expired.No))
            unmarshal[Offer](response.entity).success.value shouldBe expectedOffer
          }
        }
      }
    }

    it("should reject creation of an invalid offer") {
      val invalidOfferEntity = createOfferEntity(ValidDescription, CostZeroOrLess, now, twoDaysAgo)
      val expectedValidationErrors = ValidationRejection(Seq(
        FieldErrorInfo("cost", "offer cost must be greater than 0.00"),
        FieldErrorInfo("endDate", "offer end date must be after start date"))
      )

      Given("the server is running")
      withRunningServer {

        When("an attempt to create an invalid offer is made")
        whenReady(makeRequest("/offers", POST, invalidOfferEntity)) { response =>

          Then("the response should have status code 400 (BadRequest)")
          response.status shouldBe BadRequest

          And("the response should contain the validation errors")
          val validationErrors = unmarshal[ValidationRejection](response.entity).success.value
          validationErrors shouldBe expectedValidationErrors
        }
      }
    }

    it("should allow cancellation of an offer") {
      val entity = createOfferEntity(ValidDescription, ValidCost, now, nextDay)

      Given("the server is running")
      withRunningServer {

        And("a successful request to create an offer is made")
        whenReady(makeRequest("/offers", POST, entity)) { response =>
          response.status shouldBe Created
          val offerId = unmarshal[Offer](response.entity).success.value.offerId

          And("the offer was indeed created")
          whenReady(makeRequest(s"/offers/$offerId")) { response =>
            val expectedOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(now, nextDay), Option(Expired.No))
            response.status shouldBe OK
            unmarshal[Offer](response.entity).success.value shouldBe expectedOffer

            When("a successful request is made to cancel the created offer")
            whenReady(makeRequest(s"/offers/$offerId", DELETE)) { response =>
              response.status shouldBe OK

              Then("the offer should not be retrievable")
              whenReady(makeRequest(s"/offers/$offerId")) { response =>
                response.status shouldBe NotFound
                val responseMsg = unmarshal[String](response.entity).success.value
                responseMsg shouldBe "The requested resource could not be found but may be available again in the future."
              }
            }
          }
        }
      }
    }

    it("should allow update of an offer") {
      val entity = createOfferEntity(ValidDescription, ValidCost, now, nextDay)
      val entityUpdate = createOfferEntity(ValidDescription, ValidCost, nextThreeHours, nextDay)

      Given("the server is running")
      withRunningServer {

        And("a successful request to create an offer is made")
        whenReady(makeRequest("/offers", POST, entity)) { response =>
          response.status shouldBe Created
          val offerId = unmarshal[Offer](response.entity).success.value.offerId

          And("the offer was indeed created")
          whenReady(makeRequest(s"/offers/$offerId")) { response =>
            val originalOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(now, nextDay), Option(Expired.No))
            response.status shouldBe OK
            unmarshal[Offer](response.entity).success.value shouldBe originalOffer

            When("a successful request to update the offer is made")
            whenReady(makeRequest(s"/offers/$offerId", PUT, entityUpdate)) { response =>
              response.status shouldBe Created

              Then("the retrieved offer should reflect the updates")
              whenReady(makeRequest(s"/offers/$offerId")) { response =>
                response.status shouldBe OK
                val updatedOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(nextThreeHours, nextDay), Option(Expired.No))
                unmarshal[Offer](response.entity).success.value shouldBe updatedOffer
              }
            }
          }
        }
      }
    }

    it("should allow retrieval of all offers") {
      val firstEntity = createOfferEntity(ValidDescription, ValidCost, now, nextDay)
      val secondEntity = createOfferEntity(ValidDescription, ValidCost, nextThreeHours, nextDay)

      Given("the server is running")
      withRunningServer {
        whenReady(makeRequest("/offers")) { response =>
          response.status shouldBe OK
          unmarshal[Seq[Offer]](response.entity).success.value shouldBe Nil

          And("a successful request to create an offer is made")
          whenReady(makeRequest("/offers", POST, firstEntity)) { response =>
            response.status shouldBe Created
            val offerId = unmarshal[Offer](response.entity).success.value.offerId
            val firstOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(now, nextDay), Option(Expired.No))

            And("a successful request to create a second offer is made")
            whenReady(makeRequest("/offers", POST, secondEntity)) { response =>
              response.status shouldBe Created
              val offerId = unmarshal[Offer](response.entity).success.value.offerId
              val secondOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(nextThreeHours, nextDay), Option(Expired.No))

              When("a successful request to retrieve all offers is made")
              whenReady(makeRequest("/offers")) { response =>
                response.status shouldBe OK

                Then("the response should contain both offers")
                unmarshal[Seq[Offer]](response.entity).success.value should contain theSameElementsAs Seq(firstOffer, secondOffer)
              }
            }
          }
        }
      }
    }

    it("should allow cancellation of all offers") {
      val firstEntity = createOfferEntity(ValidDescription, ValidCost, now, nextDay)
      val secondEntity = createOfferEntity(ValidDescription, ValidCost, nextThreeHours, nextDay)

      Given("the server is running")
      withRunningServer {

        And("a successful request to create an offer is made")
        whenReady(makeRequest("/offers", POST, firstEntity)) { response =>
          response.status shouldBe Created
          val offerId = unmarshal[Offer](response.entity).success.value.offerId
          val firstOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(now, nextDay), Option(Expired.No))

          And("a successful request to create a second offer is made")
          whenReady(makeRequest("/offers", POST, secondEntity)) { response =>
            response.status shouldBe Created
            val offerId = unmarshal[Offer](response.entity).success.value.offerId
            val secondOffer = Offer(offerId, ValidDescription, Price(money(ValidCost)), Validity(nextThreeHours, nextDay), Option(Expired.No))

            And("both offers were indeed created")
            whenReady(makeRequest("/offers")) { response =>
              response.status shouldBe OK
              unmarshal[Seq[Offer]](response.entity).success.value should contain theSameElementsAs Seq(firstOffer, secondOffer)

              When("a successful request to cancel all offers is made")
              whenReady(makeRequest("/offers", DELETE)) { response =>
                response.status shouldBe OK

                Then("there should be no offers retrievable")
                whenReady(makeRequest("/offers")) { response =>
                  response.status shouldBe OK
                  unmarshal[Seq[Offer]](response.entity).success.value shouldBe Nil
                }
              }
            }
          }
        }
      }
    }
  }

  private def randomAvailablePort(): Int = new ServerSocket(0).getLocalPort

  private def makeRequest(url: String, method: HttpMethod = GET, entity: RequestEntity = HttpEntity.Empty)
                         (implicit actorSystem: ActorSystem, materializer: ActorMaterializer) = {
    Http().singleRequest(HttpRequest(method, Uri(s"http://$ApiHost:$ApiPort$url"), entity = entity))
  }

  private def withRunningServer(test: => Assertion): Assertion = {
    val cache: Cache[String, Offer] = Scaffeine()
      .maximumSize(OfferCacheCapacity)
      .build[String, Offer]()

    val offerRepository = new DefaultOfferRepository(cache)
    val offerService = new DefaultOfferService(offerRepository)
    val server = new Server(offerService)

    try {
      server.startServer(ApiHost, ApiPort, ServerSettings(config), Option(ActorSystem("server")))
      test
    } finally server.terminate()
  }

}
