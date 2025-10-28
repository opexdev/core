package co.nilin.opex.profile.app.utils

import co.nilin.opex.profile.app.dto.AddressBookResponse
import co.nilin.opex.profile.app.dto.BankAccountResponse
import co.nilin.opex.profile.core.data.profile.AddressBook
import co.nilin.opex.profile.core.data.profile.BankAccount

fun BankAccount.toBankAccountResponse() = BankAccountResponse(
    id = this.id,
    name = this.name,
    cardNumber = this.cardNumber,
    iban = this.iban,
    accountNumber = this.accountNumber,
    bank = this.bank,
    status = this.status
)

fun AddressBook.toAddressBookResponse() = AddressBookResponse(
    id = this.id,
    name = this.name,
    address = this.address,
    addressType = this.addressType,
)
