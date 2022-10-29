package com.lohanna.paymentreceipt

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BankSlip(
    @SerializedName("boleto") var bankSlip: BankSlipData? = null
)

data class BankSlipData(
    @SerializedName("dataHoraGeradoTimestamp") var generatedDateTime: Long? = null,
    @SerializedName("dataVencimentoTimestamp") var dueDate: Long? = null,
    @SerializedName("linhaDigitavel") var barcodeNumber: String? = null,
    @SerializedName("valorPagar") var paymentValue: BigDecimal? = null
)