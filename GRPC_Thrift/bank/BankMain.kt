package bank

import kotlin.system.exitProcess

fun main(args : Array<String>) {
    /*
    Usage:
    arg0: bank port
    arg1: currency host
    arg2: currency port
     */
    if (args.size != 3){
        println("Wrong number of arguments")
        exitProcess(1)
    }

    val currencyHost = args[1]

    val portNumber:Int
    val currencyPortNumber:Int

    try {
        portNumber = args[0].toInt()
        currencyPortNumber = args[2].toInt()
    }
    catch (e: NumberFormatException){
        println("Illegal argument")
        exitProcess(1)
    }

    val bankServer = BankServer(portNumber, currencyHost, currencyPortNumber)
    bankServer.launch()

}