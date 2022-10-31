package com.lohanna.paymentreceipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.lohanna.paymentreceipt.databinding.ActivityMainBinding
import io.github.muddz.styleabletoast.StyleableToast
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        val data = readFile()

        binding.apply {
            val dateTime = data.bankSlip?.generatedDateTime?.let {
                getDateTime(it, "dd/MM/yyyy, HH:mm")
            }
            val dueDate = data.bankSlip?.dueDate?.let { getDateTime(it, "dd/MM/yyyy") }
            val value = data.bankSlip?.paymentValue?.let { getFormattedValue(it) }
            val barcodeNumber = data.bankSlip?.barcodeNumber

            tvDatetime.text = dateTime
            tvValue.text = getString(R.string.value, value)
            tvDate.text = dueDate

            btnCopy.setOnClickListener {
                if(barcodeNumber != null) {
                    copyToClipboard(barcodeNumber)

                    StyleableToast.makeText(
                        this@MainActivity,
                        getString(R.string.toast_message),
                        Toast.LENGTH_SHORT,
                        R.style.mytoast
                    ).show()
                }

            }
        }
    }

    private fun readFile(): BankSlip {
        lateinit var jsonString: String

        try {
            jsonString = this.assets.open("boleto_response.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        return Gson().fromJson(jsonString, BankSlip::class.java)
    }

    private fun getDateTime(input: Long, format: String): String? {
        val dateFormat = SimpleDateFormat(
            format,
            Locale("pt", "BR")
        )

        val date = Date(input * 1000)

        return dateFormat.format(date)
    }

    private fun getFormattedValue(value: BigDecimal): String {
        val numberFormat = NumberFormat.getInstance(
            Locale("pt", "BR")
        )

        return numberFormat.format(value)
    }

    private fun copyToClipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

}