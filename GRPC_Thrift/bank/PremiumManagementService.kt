package bank

import grpc.currency.Currency
import rpc.bank.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PremiumManagementService(private var accounts: ConcurrentHashMap<GUID, UserAcount>, private var currencies: ConcurrentHashMap<String, Currency>) : PremiumManagement.Iface {
    override fun getLoanInfo(loanRequest: LoanRequest?): LoanResponse {
        println("Account with id: ${loanRequest!!.pesel} wants take ${loanRequest.value} in ${loanRequest.currency}.")

        var userAccount: UserAcount? = null
        for (account in accounts.values){
            if (account.pesel == loanRequest.pesel) {
                userAccount = account
                break
            }
        }
        userAccount ?: throw AccountNotExists()

        if (userAccount.type == AccountType.STANDARD) throw InvalidAccountType()

        val time = calculateLoanTime(loanRequest)
        val otherCurrency = currencies[loanRequest.currency]

        otherCurrency ?: throw UnavailableCurrency()

        val plnRRSO = 0.3
        val otherRRSO = 0.2

        val parentValue = loanRequest.value + (loanRequest.value * plnRRSO * time)
        val otherValue = (loanRequest.value + (loanRequest.value * otherRRSO * time)) / otherCurrency.sell

        val parentValueTrimmed = DecimalFormat("##.##").format(parentValue)
        val otherValueTrimmed = DecimalFormat("##.##").format(otherValue)

        println("Account with id: ${loanRequest.pesel} can take ${loanRequest.value} in ${loanRequest.currency}. " +
                "Cost is $parentValueTrimmed in PLN and $otherValueTrimmed in ${otherCurrency.type}")

        return LoanResponse().setParentValue(parentValue).setOtherValue(otherValue)


    }

    private fun calculateLoanTime(loanRequest: LoanRequest): Double {
        val startDate = GregorianCalendar(loanRequest.startDay.year, loanRequest.startDay.month - 1, loanRequest.startDay.day)
        val endDate = GregorianCalendar(loanRequest.endDay.year, loanRequest.endDay.month - 1, loanRequest.endDay.day)

        val millisInYear = 1000 * 60 * 60 * 24 * 365.0
        return (endDate.timeInMillis - startDate.timeInMillis) / millisInYear
    }

    override fun getState(pesel: String?): Double {
        println("Premium account with id: $pesel gets its state.")
        for (account in accounts.values){
            if (account.pesel == pesel) {
                return account.state
            }
        }
        throw AccountNotExists()
    }

}