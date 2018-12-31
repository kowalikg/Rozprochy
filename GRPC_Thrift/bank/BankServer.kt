package bank

import grpc.currency.CurrenciesStreamGrpc
import grpc.currency.Currency
import grpc.currency.CurrencyTypes
import grpc.currency.Type
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.apache.thrift.TMultiplexedProcessor
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.server.TThreadPoolServer
import org.apache.thrift.transport.TServerSocket
import rpc.bank.*
import java.util.concurrent.ConcurrentHashMap


class BankServer(private val portNumber: Int, currencyHost: String, currencyPortNumber: Int) {
    private var channel: ManagedChannel = ManagedChannelBuilder.forAddress(currencyHost, currencyPortNumber)
            .usePlaintext(true)
            .build()
    private var streamTesterBlockingStub: CurrenciesStreamGrpc.CurrenciesStreamBlockingStub
    private val currenciesMap = ConcurrentHashMap<String, Currency>()
    private val accounts = ConcurrentHashMap<GUID, UserAcount>()

    init {
        streamTesterBlockingStub = CurrenciesStreamGrpc.newBlockingStub(channel)
    }

    fun launch() {
        Thread(AccountServiceLauncher(accounts, currenciesMap, portNumber)).start()
        launchCurrencyService()
    }

    private class AccountServiceLauncher(val accounts: ConcurrentHashMap<GUID, UserAcount>,
                                         val currencies: ConcurrentHashMap<String, Currency>,
                                         val portNumber: Int) : Runnable {
        override fun run() {
            val accountManagementService = AccountManagementService(accounts)
            val standardManagementService = StandardManagementService(accounts)
            val premiumManagementService = PremiumManagementService(accounts, currencies)

            val accountManagementProcessor = AccountManagment.Processor(accountManagementService)
            val standardManagementProcessor = StandardManagement.Processor(standardManagementService)
            val premiumManagementProcessor = PremiumManagement.Processor(premiumManagementService)

            val serverTransport = TServerSocket(portNumber)

            val protocolFactory = TBinaryProtocol.Factory()

            val tMultiplexedProcessor = TMultiplexedProcessor()
            tMultiplexedProcessor.registerProcessor("AccountManagementService", accountManagementProcessor)
            tMultiplexedProcessor.registerProcessor("StandardManagementService", standardManagementProcessor)
            tMultiplexedProcessor.registerProcessor("PremiumManagementService", premiumManagementProcessor)

            val server = TThreadPoolServer(TThreadPoolServer.Args(serverTransport).protocolFactory(protocolFactory).processor(tMultiplexedProcessor))

            println("Bank-Client service started at port $portNumber")
            server.serve()
        }

    }

    private fun launchCurrencyService() {
        val currenciesToRand = populateCurrencies()
        val firstCurrency = randCurrency(currenciesToRand)
        val secondCurrency = randCurrency(currenciesToRand)

        val currenciesTitle = "Currencies: $firstCurrency, $secondCurrency"
        println(currenciesTitle)

        val usedCurrencies = CurrencyTypes.newBuilder().addType(firstCurrency).addType(secondCurrency).build()

        val currenciesIterator = streamTesterBlockingStub.getCurrencies(usedCurrencies)

        while (currenciesIterator.hasNext()){

            val currencies = currenciesIterator.next().currencyList
            for (currency in currencies){
                currenciesMap[currency.type.toString()] = currency
                println("${currency.type}, buy: ${currency.buy}, sell: ${currency.sell}")
            }
       }
    }

    private fun randCurrency(currenciesToRand: ArrayList<Type>): Type {
        currenciesToRand.shuffle()

        val currency = currenciesToRand[0]
        currenciesToRand.removeAt(0)

        return currency
    }

    private fun populateCurrencies(): ArrayList<Type> {
        val currenciesToRand = ArrayList<Type>()
        currenciesToRand.add(Type.AUD)
        currenciesToRand.add(Type.JPY)
        currenciesToRand.add(Type.HUF)
        currenciesToRand.add(Type.SEK)

        return currenciesToRand
    }
}
