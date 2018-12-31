package currency

import io.grpc.Server
import io.grpc.ServerBuilder

class CurrencyServiceServer(private val portNumber: Int) {
    private var server: Server? = null
    private var currencyDB = CurrencyDB()
    fun launch() {
        server = ServerBuilder
                .forPort(portNumber)
                .addService(CurrencyServiceImpl(currencyDB))
                .build()
                .start()

        println("Started service at port $portNumber")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@CurrencyServiceServer.stop()
                println("*** server shut down")
            }
        })

    }

    private fun stop() {
        if (server != null) {
            server!!.shutdown()
        }
    }

    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        if (server != null) {
            server!!.awaitTermination()
        }
    }
}