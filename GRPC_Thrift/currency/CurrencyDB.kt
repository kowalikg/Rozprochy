package currency

import grpc.currency.CurrencyTypes
import grpc.currency.Type
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class CurrencyDB {
    private var currencies = CopyOnWriteArrayList<CurrencyObject>()

    init {
        currencies.add(CurrencyObject(Type.PLN, 1.0, 1.0))
        currencies.add(CurrencyObject(Type.SEK, 0.4090, 0.4190))
        currencies.add(CurrencyObject(Type.AUD, 2.5677, 2.7674))
        currencies.add(CurrencyObject(Type.HUF, 0.0102, 0.0133))
        currencies.add(CurrencyObject(Type.JPY, 0.0308, 0.0338))
    }

    fun desirableCurrencies(currencyTypes: CurrencyTypes): List<CurrencyObject> {
        val selectedTypes = currencyTypes.typeList

        val selectedCurrencies = ArrayList<CurrencyObject>()

        for (currency in currencies) {
            if (selectedTypes.contains(currency.type))
                selectedCurrencies.add(changeCurrencyRate(currency))
        }
        return selectedCurrencies


    }

    private fun changeCurrencyRate(currencyObject: CurrencyObject): CurrencyObject {
        if (currencyObject.type == Type.PLN) return currencyObject
        val min = 96
        val max = 104
        val percentage = Random().nextFloat() * (max - min) + min

        val buy:Double = (currencyObject.buy * percentage) / 100
        val sell:Double = (currencyObject.sell * percentage) / 100

        return CurrencyObject(currencyObject.type, buy, sell)
    }
}
