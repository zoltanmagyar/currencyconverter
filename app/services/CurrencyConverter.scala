package services

import java.math.RoundingMode

import javax.inject.Inject
import model.{ConversionResponse, ExchangeRatesResponse}
import org.joda.money.{CurrencyUnit, Money}
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

sealed trait ConversionError
case object ExchangeRateNotFound extends Throwable with ConversionError
case class FailedConversion(exception: Throwable) extends ConversionError

trait CurrencyConverter {
  def convert(fromCurrency: CurrencyUnit,
              toCurrency: CurrencyUnit,
              amount: BigDecimal): Future[Either[ConversionError, ConversionResponse]]
}
/**
  * CurrencyConverter implementation which uses a cache. Cache entries are invalidated after 1 minute.
  */
class CachingCurrencyConverter @Inject()(cache: AsyncCacheApi, exchangeRatesClient: ExchangeRatesClient)
                                        (implicit ec: ExecutionContext) extends CurrencyConverter {
  override def convert(fromCurrency: CurrencyUnit,
                       toCurrency: CurrencyUnit,
                       amount: BigDecimal): Future[Either[ConversionError, ConversionResponse]] = {
    val key = s"${fromCurrency.getCode}->${toCurrency.getCode}"
    cache.getOrElseUpdate(key, expiration = 1.minute)(exchangeRatesClient.refresh(fromCurrency, toCurrency)).map { rate =>
      val moneyToConvert = Money.of(fromCurrency, amount.bigDecimal)
      Try(moneyToConvert.convertedTo(toCurrency, rate.bigDecimal, RoundingMode.HALF_UP)) match {
        case Success(convertedMoney) =>
          Right(ConversionResponse(exchange = rate, amount = BigDecimal(convertedMoney.getAmount), original = amount))
        case Failure(exception) =>
          // log the error
          Left(FailedConversion(exception))
      }
    }.recover {
      case ExchangeRateNotFound =>
        Left(ExchangeRateNotFound)
    }
  }
}

class ExchangeRatesClient @Inject()(config: Configuration, ws: WSClient)(implicit ec: ExecutionContext) {
  def refresh(from: CurrencyUnit, to: CurrencyUnit): Future[BigDecimal] = {
    val requestTimeout = 3.seconds
    val exchangeRatesRequest = ws.url(
      s"${config.get[String]("exchangeRatesUrl")}/latest?base=${from.getCode}"
    ).withRequestTimeout(requestTimeout).get()
    exchangeRatesRequest.flatMap { exchangeRatesResponse =>
      Try(exchangeRatesResponse.json.as[ExchangeRatesResponse]) match {
        case Failure(exception) =>
          Future.failed(exception)
        case Success(response) =>
          response.rates.get(to) match {
            case None =>
              Future.failed(ExchangeRateNotFound)
            case Some(rate) =>
              Future.successful(rate)
          }
      }
    }
  }
}
