package controllers

import javax.inject.{Inject, Singleton}
import model.ConversionRequest
import play.api.libs.json.{Format, Json}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import services.{CurrencyConverter, ExchangeRateNotFound, FailedConversion}

import scala.concurrent.ExecutionContext

/**
  * Controller with a single endpoint that handles currency conversions
  */
@Singleton
class Controller @Inject()(cc: ControllerComponents, currencyConverter: CurrencyConverter)
                          (implicit exec: ExecutionContext) extends AbstractController(cc) {
  def convert = Action.async(parse.json[ConversionRequest]) { implicit request =>
    currencyConverter.convert(request.body.fromCurrency, request.body.toCurrency, request.body.amount).map(_.fold(
      {
        case ExchangeRateNotFound => NotFound(toJson(ErrorCode("EXCHANGE_RATE_NOT_FOUND"))).as(JSON)
        case FailedConversion(_) => InternalServerError
      },
      { conversionResponse => Ok(toJson(conversionResponse)).as(JSON) }
    ))
  }
}

case class ErrorCode(code: String)

object ErrorCode {
  implicit val errorCodeFormat: Format[ErrorCode] = Json.format[ErrorCode]
}
