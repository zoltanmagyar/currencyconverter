import akka.Done
import org.joda.money.CurrencyUnit
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.AsyncCacheApi
import services.{CachingCurrencyConverter, ExchangeRateNotFound, ExchangeRatesClient, FailedConversion}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.reflect.ClassTag

class CurrencyConverterTest extends PlaySpec with MockitoSugar {

  val Timeout: Duration = 5.millis

  class MockCache[T](value: Future[T]) extends AsyncCacheApi {
    override def set(key: String, value: Any, expiration: Duration): Future[Done] = ???
    override def remove(key: String): Future[Done] = ???
    override def getOrElseUpdate[A](key: String, expiration: Duration)(orElse: => Future[A])
                                   (implicit evidence$1: ClassTag[A]): Future[A] = value.map(_.asInstanceOf[A])
    override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Future[Option[T]] = ???
    override def removeAll(): Future[Done] = ???
  }

  "CurrencyConverter" should {
    "convert one currency to another" in {
      val fromCurrency = CurrencyUnit.GBP
      val toCurrency = CurrencyUnit.EUR
      val amount = 1
      val rate: BigDecimal = 1.13
      val mockCache = new MockCache(Future.successful(rate))

      val result = Await.result(
        new CachingCurrencyConverter(mockCache, mock[ExchangeRatesClient]).convert(fromCurrency, toCurrency, amount),
        Timeout
      )

      result must be ('right)
      val conversion = result.right.get
      conversion.exchange mustEqual rate
      conversion.amount mustEqual amount * rate
      conversion.original mustEqual amount
    }
    "fail if rate is invalid" in {
      val fromCurrency = CurrencyUnit.GBP
      val toCurrency = CurrencyUnit.EUR
      val amount = 1
      val rate: BigDecimal = -1.13
      val mockCache = new MockCache(Future.successful(rate))

      val result = Await.result(
        new CachingCurrencyConverter(mockCache, mock[ExchangeRatesClient]).convert(fromCurrency, toCurrency, amount),
        Timeout
      )

      result must be ('left)
      val error = result.left.get
      error mustBe a [FailedConversion]
    }
    "fail if target currency rate is not available" in {
      val fromCurrency = CurrencyUnit.GBP
      val toCurrency = CurrencyUnit.EUR
      val amount = 1
      val mockCache = new MockCache(Future.failed(ExchangeRateNotFound))

      val result = Await.result(
        new CachingCurrencyConverter(mockCache, mock[ExchangeRatesClient]).convert(fromCurrency, toCurrency, amount),
        Timeout
      )

      result must be ('left)
      val error = result.left.get
      error mustEqual ExchangeRateNotFound
    }
  }
}
