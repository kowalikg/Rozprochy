package bank

import rpc.bank.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AccountManagementService(private var accounts: ConcurrentHashMap<GUID, UserAcount>) : AccountManagment.Iface {

    override fun login(guid: GUID?): UserAcount {
        println("Account with guid $guid wants to login.")
        return accounts[guid] ?: throw AccountNotExists()
    }

    override fun create(newAccount: UserAcount?): GUID {
        println("Creating new account")

        for (account in accounts.values){
            if (account.pesel == newAccount!!.pesel)
                throw AccountExists()
        }

        val guid = GUID().setSequence(UUID.randomUUID().toString())
        val accountType = if (newAccount!!.income >= 1000) AccountType.PREMIUM else AccountType.STANDARD
        newAccount.type = accountType
        accounts[guid] = newAccount

        println("Account created, guid: $guid")
        return guid
    }



}