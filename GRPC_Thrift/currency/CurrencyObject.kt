package currency

import grpc.currency.Type

class CurrencyObject(val type: Type, val buy: Double, val sell: Double) {

}