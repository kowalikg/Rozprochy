from thrift.protocol import TBinaryProtocol
from thrift.protocol.TMultiplexedProtocol import TMultiplexedProtocol
from thrift.transport import TSocket, TTransport

from bank import AccountManagment, StandardManagement, PremiumManagement
from bank.ttypes import UserAcount, AccountExists, AccountNotExists, GUID, AccountType, Date, LoanRequest, \
    InvalidAccountType, UnavailableCurrency

import sys


class Client:
    def __init__(self, bank_port):
        self.bank_port = bank_port
        self.account_management = None
        self.standard_management = None
        self.premium_management = None

    def launch(self):

        transport = TTransport.TBufferedTransport(TSocket.TSocket('localhost', self.bank_port))
        transport.open()

        protocol = TBinaryProtocol.TBinaryProtocol(transport)

        self.account_management = AccountManagment.Client(TMultiplexedProtocol(protocol, "AccountManagementService"))
        self.standard_management = StandardManagement.Client(
            TMultiplexedProtocol(protocol, "StandardManagementService"))
        self.premium_management = PremiumManagement.Client(TMultiplexedProtocol(protocol, "PremiumManagementService"))

        print("Client started, bank port: " + str(self.bank_port))

        self.start()

        transport.close()

    def start(self):
        while True:
            user_input = input("Usage: \n"
                               "N - create new account\n"
                               "L - login \n"
                               "Q - quit\n")

            if user_input == "Q":
                break
            elif user_input == "N":
                self.create_account()
            elif user_input == "L":
                self.login()
            else:
                print("Invalid option")

    def create_account(self):
        user_account = UserAcount()

        user_account.name = input("Write your name: ")
        user_account.lastname = input("Write your last name: ")
        user_account.pesel = input("Write your PESEL: ")
        user_account.income = float(input("Write your monthly income: "))
        user_account.state = float(input("How much money do you put?: "))

        try:
            guid = self.account_management.create(user_account)
            print("Your guid: " + guid.sequence + "\n"
                                                  "Use it as your login\n"
                  )
        except AccountExists:
            print("Account with this pesel exists!")

    def login(self):
        guid = input("Write your guid: ")
        try:
            account = self.account_management.login(GUID(guid))
            print("Logged successfully")
            print("Welcome " + account.name + " " + account.lastname)
            user = "standard" if account.type == 0 else "premium"
            print("You are " + user + " user")
            if account.type == AccountType.STANDARD:
                self.standard_logged_mode(account)
            else:
                self.premium_logged_mode(account)
        except AccountNotExists:
            print("Account with this guid not exists!")

    def standard_logged_mode(self, account):
        while True:
            user_input = input("Usage: \n"
                               "S - check account state\n"
                               "L - logout \n"
                               "Q - quit\n")
            if user_input == "Q":
                sys.exit(0)
            elif user_input == "L":
                break
            elif user_input == "S":
                print("Account state: " + str(self.standard_management.getState(account.pesel)))
            else:
                print("Invalid option")

    def premium_logged_mode(self, account):
        while True:
            user_input = input("Usage: \n"
                               "S - check account state\n"
                               "C - credit info\n"
                               "L - logout \n"
                               "Q - quit\n")
            if user_input == "Q":
                sys.exit(0)
            elif user_input == "L":
                break
            elif user_input == "S":
                print("Account state: " + str(self.premium_management.getState(account.pesel)))
            elif user_input == "C":
                self.credit_info(account)
            else:
                print("Invalid option")

    def credit_info(self, account):
        value = float(input("How much do you want to take?\n"))
        while True:
            currency = input("In which other currency are you interested?\n"
                             + 'AUD, SEK, HUF, JPY\n')
            if currency in ("AUD", "SEK", "HUF", "JPY"):
                break

        start_date = Date()

        print("Write start credit day:")
        start_date.day = int(input("Write day: \n"))
        start_date.month = int(input("Write month: \n"))
        start_date.year = int(input("Write year: \n"))

        end_date = Date()

        print("Write end credit day:")

        end_date.day = int(input("Write day: \n"))
        end_date.month = int(input("Write month: \n"))
        end_date.year = int(input("Write year: \n"))

        loan_request = LoanRequest()
        loan_request.pesel = account.pesel
        loan_request.value = value
        loan_request.currency = currency
        loan_request.startDay = start_date
        loan_request.endDay = end_date

        try:
            loan_info = self.premium_management.getLoanInfo(loan_request)
            print("Loan cost in PLN: " + str(float("{0:.2f}".format(loan_info.parentValue))))
            print("Loan cost in " + currency + ": " + str(float("{0:.2f}".format(loan_info.otherValue))))
        except AccountNotExists:
            print('Account not exist')
        except InvalidAccountType:
            print('Invalid account type')
        except UnavailableCurrency:
            print('Currency not available')
