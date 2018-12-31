package bank

import rpc.bank.AccountNotExists
import rpc.bank.GUID
import rpc.bank.StandardManagement
import rpc.bank.UserAcount
import java.util.concurrent.ConcurrentHashMap

class StandardManagementService(private val accounts: ConcurrentHashMap<GUID, UserAcount>) : StandardManagement.Iface {
    override fun getState(pesel: String?): Double  {
        println("Standard account with id: $pesel gets its state.")
        for (account in accounts.values){
            if (account.pesel == pesel) {
                return account.state
            }
        }
        println("No account with id: $pesel.")
        throw AccountNotExists()
    }
}