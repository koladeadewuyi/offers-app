package com.kolade.offers.validation

import com.kolade.offers.TestFixture
import com.kolade.offers.model.{FieldErrorInfo, Offer, Validity}

class OfferValidatorSpec extends TestFixture {

  describe("OfferValidator") {
    describe("apply") {
      it("should return an empty list when given a valid offer") {
        val validOffer = Offer(randomUUID, ValidDescription, price(ValidAmount), Validity(now, nextDay))

        val validationErrors = OfferValidator.apply(validOffer)

        validationErrors shouldBe Nil
      }

      val errorScenarios = Table(
        ("testDescription", "offerId", "description", "amount", "startDate", "endDate", "invalidFieldToErrorMsgs"),
        ("offerId is not > 10 chars", InvalidOfferId, ValidDescription, ValidAmount, now, nextDay, Seq(("offerId", "offerId must exceed 10 characters"))),
        ("description < 20 chars", randomUUID, "text < 20 chars", ValidAmount, now, nextDay, Seq(("description", "offer description must exceed 20 characters"))),
        ("price is not > 0", randomUUID, ValidDescription, AmountZeroOrLess, now, nextDay, Seq(("price", "offer price must be greater than 0.00"))),
        ("startDate is earlier than previous day", randomUUID, ValidDescription, ValidAmount, twoDaysAgo, nextDay, Seq(("startDate", "offer start date must not be earlier than 1 day(s) ago"))),
        ("endDate is before startDate", randomUUID, ValidDescription, ValidAmount, now, now.minusMillis(1), Seq(("endDate", "offer end date must be after start date"))),
        ("multiple invalid fields i.e. offerId is not > 10 chars and price is not > 0", InvalidOfferId, ValidDescription, AmountZeroOrLess, now, nextDay, Seq(("offerId", "offerId must exceed 10 characters"), ("price", "offer price must be greater than 0.00")))
      )

      forAll(errorScenarios) { (testDescription, offerId, description, amount, startDate, endDate, invalidFieldToErrorMsgs) =>
        it(s"should return a list of corresponding error info when $testDescription") {
          withSystemTimeSetTo(now) {
            val validOffer = Offer(offerId, description, price(amount), Validity(startDate, endDate))
            val expectedValidationErrors = invalidFieldToErrorMsgs.map {
              case (invalidField, errorMsg) => FieldErrorInfo(invalidField, errorMsg)
            }

            val validationErrors = OfferValidator.apply(validOffer)

            validationErrors should contain theSameElementsAs expectedValidationErrors
          }
        }
      }
    }
  }

}
