package currency
import grpc.currency.Currencies
import grpc.currency.CurrenciesStreamGrpc
import grpc.currency.Currency
import grpc.currency.CurrencyTypes
import io.grpc.stub.StreamObserver

class CurrencyServiceImpl(private val currencyDB: CurrencyDB, private val sleepTime: Int = 5000) : CurrenciesStreamGrpc.CurrenciesStreamImplBase() {
    override fun getCurrencies(request: CurrencyTypes?, responseObserver: StreamObserver<Currencies>?) {
        println("Started streaming for bank")
        while (true){
            val currenciesBuilder = Currencies.newBuilder()
            val selectedCurrencies = currencyDB.desirableCurrencies(request!!)

            for (currency in selectedCurrencies){
                val grpcCurrency = Currency.newBuilder()
                        .setType(currency.type)
                        .setBuy(currency.buy)
                        .setSell(currency.sell)
                        .build()
                currenciesBuilder.addCurrency(grpcCurrency)
            }

            val currencies = currenciesBuilder.build()
            responseObserver?.onNext(currencies)


            try {
                Thread.sleep(sleepTime.toLong())
            }
            catch (e: InterruptedException){
                println("Problem with streaming service")
                break
            }

        }
        responseObserver!!.onCompleted()
    }

}