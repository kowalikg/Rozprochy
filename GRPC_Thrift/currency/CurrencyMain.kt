package currency

import kotlin.system.exitProcess

fun main(args : Array<String>) {
    /*
    Usage:
    arg0: port number
     */
    if (args.size != 1){
        println("No port number defined in program arguments")
        exitProcess(1)
    }
    val portNumber:Int
    try {
        portNumber = args[0].toInt()
    }
    catch (e: NumberFormatException){
        println("Illegal argument")
        exitProcess(1)
    }


    val currencyServer = CurrencyServiceServer(portNumber)
    currencyServer.launch()
    currencyServer.blockUntilShutdown()
    println("Currency server started at port $portNumber")
}