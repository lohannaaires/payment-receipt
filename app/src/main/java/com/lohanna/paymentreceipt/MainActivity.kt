package com.lohanna.paymentreceipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.lohanna.paymentreceipt.databinding.ActivityMainBinding
import io.github.muddz.styleabletoast.StyleableToast
import java.io.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fileName = "boleto_base64.json"
    private var data = ""
    private var fileType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        val jsonString = readFile("boleto_response.json")
        val data = Gson().fromJson(jsonString, BankSlip::class.java)

        val dateTime = data.bankSlip?.generatedDateTime?.let {
            getDateTime(it, "dd/MM/yyyy, HH:mm")
        }
        val dueDate = data.bankSlip?.dueDate?.let { getDateTime(it, "dd/MM/yyyy") }
        val value = data.bankSlip?.paymentValue?.let { getFormattedValue(it) }
        val barcodeNumber = data.bankSlip?.barcodeNumber

        binding.apply {
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

            btnOpenDocument.setOnClickListener {
                openPdfFile()
            }

            btnShare.setOnClickListener {
                sharePdfFile()
            }
        }
    }

    private fun readFile(fileName: String): String {
        lateinit var jsonString: String

        try {
            jsonString = this.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        return jsonString
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

    private fun openPdfFile() {
        val base64Data = deserializeJsonData()

        base64Data.file?.data?.let { data = it }
        base64Data.file?.mime?.let { fileType = it }

        val file = generatePdfFile(data)

        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(file, fileType)
        pdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(pdfIntent)
    }

    private fun sharePdfFile() {
        val base64Data = deserializeJsonData()

        base64Data.file?.data?.let { data = it }
        base64Data.file?.mime?.let { fileType = it }

        val file = generatePdfFile(data)

        val share = Intent(Intent.ACTION_SEND)
        share.putExtra(Intent.EXTRA_STREAM, file)
        share.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        share.type = fileType
        startActivity(Intent.createChooser(share, "Share via..."))
    }

    private fun deserializeJsonData(): BankSlipBase64 {
        val jsonString = readFile(fileName)
        return Gson().fromJson(jsonString, BankSlipBase64::class.java)
    }

    private fun generatePdfFile(data: String): Uri {
        val file = File(this.cacheDir, "boleto.pdf")

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        val fos = FileOutputStream(file)
        val pdfBytes = Base64.decode(data, Base64.DEFAULT)
        fos.write(pdfBytes)
        fos.flush()
        fos.close()

        return FileProvider.getUriForFile(
            this, applicationContext.packageName + ".provider", file
        )
    }

}