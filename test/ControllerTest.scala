import controllers.Controller
import model.{ConversionRequest, ConversionResponse}
import org.joda.money.CurrencyUnit
import org.scalatestplus.play._
import play.api.http.HeaderNames
import play.api.libs.json.Json.toJson
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import services.{CurrencyConverter, ExchangeRateNotFound, FailedConversion}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ControllerTest extends PlaySpec {

  "Controller" should {
    val conversionRequest = ConversionRequest(CurrencyUnit.GBP, CurrencyUnit.EUR, 1)
    def fakeRequest(conversionRequest: ConversionRequest) =
      FakeRequest(GET, "/api/convert", FakeHeaders(Seq(HeaderNames.HOST -> "localhost")), conversionRequest)

    "return a valid result with action" in {
      val conversionResponse = ConversionResponse(1.13, 1.13, 1)
      val currencyConverter: CurrencyConverter = (_: CurrencyUnit, _: CurrencyUnit, _: Double) =>
        Future.successful(Right(conversionResponse))
      val controller = new Controller(stubControllerComponents(), currencyConverter)
      val result = controller.convert(fakeRequest(conversionRequest))
      status(result) must equal(OK)
      contentAsJson(result) must equal(toJson(conversionResponse))
    }

    "return an internal server error result" in {
      val currencyConverter: CurrencyConverter = (_: CurrencyUnit, _: CurrencyUnit, _: Double) =>
        Future.successful(Left(FailedConversion(new IllegalArgumentException())))
      val controller = new Controller(stubControllerComponents(), currencyConverter)
      val result = controller.convert(fakeRequest(conversionRequest))
      status(result) must equal(INTERNAL_SERVER_ERROR)
    }

    "return a not found error result" in {
      val currencyConverter: CurrencyConverter = (_: CurrencyUnit, _: CurrencyUnit, _: Double) =>
        Future.successful(Left(ExchangeRateNotFound))
      val controller = new Controller(stubControllerComponents(), currencyConverter)
      val result = controller.convert(fakeRequest(conversionRequest))
      status(result) must equal(NOT_FOUND)
    }
  }
}
