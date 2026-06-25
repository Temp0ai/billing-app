package com.billing.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.billing.app.data.entity.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.util.DateUtils
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40f

    private val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    private val headerPaint = Paint().apply {
        textSize = 12f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    private val bodyPaint = Paint().apply {
        textSize = 10f
        color = android.graphics.Color.BLACK
    }

    private val smallPaint = Paint().apply {
        textSize = 8f
        color = android.graphics.Color.GRAY
    }

    private val linePaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 0.5f
    }

    fun generateInvoicePdf(
        invoice: Invoice,
        items: List<InvoiceItem>,
        businessProfile: BusinessProfile,
        fileName: String? = null
    ): File {
        val document = PdfDocument()
        var pageNumber = 1
        var currentY = margin

        // Page 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Header - Business Info
        canvas.drawText(businessProfile.businessName, margin, currentY + 20, titlePaint)
        currentY += 30

        if (businessProfile.gstin.isNotEmpty()) {
            canvas.drawText("GSTIN: ${businessProfile.gstin}", margin, currentY, bodyPaint)
            currentY += 15
        }

        val addressLines = listOfNotNull(
            businessProfile.address.takeIf { it.isNotEmpty() },
            "${businessProfile.city}, ${businessProfile.state} - ${businessProfile.pincode}".takeIf { businessProfile.city.isNotEmpty() },
            "Phone: ${businessProfile.phone}".takeIf { businessProfile.phone.isNotEmpty() },
            "Email: ${businessProfile.email}".takeIf { businessProfile.email.isNotEmpty() }
        )

        for (line in addressLines) {
            canvas.drawText(line, margin, currentY, bodyPaint)
            currentY += 14
        }

        currentY += 10

        // Invoice Title
        val titleText = when (invoice.invoiceType) {
            InvoiceType.TAX_INVOICE -> "TAX INVOICE"
            InvoiceType.PROFORMA -> "PROFORMA INVOICE"
            InvoiceType.QUOTATION -> "QUOTATION"
            InvoiceType.DELIVERY_CHALLAN -> "DELIVERY CHALLAN"
            InvoiceType.CREDIT_NOTE -> "CREDIT NOTE"
            InvoiceType.DEBIT_NOTE -> "DEBIT NOTE"
        }
        canvas.drawText(titleText, pageWidth / 2 - 50, currentY, titlePaint)
        currentY += 25

        // Divider
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 15

        // Invoice Details - Left side
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", margin, currentY, headerPaint)
        canvas.drawText("Date: ${DateUtils.formatDate(invoice.invoiceDate)}", pageWidth - margin - 120, currentY, headerPaint)
        currentY += 18

        if (invoice.dueDate > 0) {
            canvas.drawText("Due Date: ${DateUtils.formatDate(invoice.dueDate)}", pageWidth - margin - 120, currentY, bodyPaint)
            currentY += 18
        }

        // Bill To
        currentY += 5
        canvas.drawText("Bill To:", margin, currentY, headerPaint)
        currentY += 16
        canvas.drawText(invoice.partyName, margin, currentY, bodyPaint)
        currentY += 14

        if (invoice.partyGstin.isNotEmpty()) {
            canvas.drawText("GSTIN: ${invoice.partyGstin}", margin, currentY, bodyPaint)
            currentY += 14
        }

        if (invoice.partyAddress.isNotEmpty()) {
            canvas.drawText(invoice.partyAddress, margin, currentY, bodyPaint)
            currentY += 14
        }

        if (invoice.placeOfSupply.isNotEmpty()) {
            canvas.drawText("Place of Supply: ${invoice.placeOfSupply}", margin, currentY, bodyPaint)
            currentY += 14
        }

        currentY += 10

        // Items Table
        val colWidths = floatArrayOf(30f, 180f, 50f, 60f, 60f, 60f, 60f)
        val colHeaders = arrayOf("#", "Item", "Qty", "Rate", "Tax", "GST", "Amount")

        // Table header background
        val headerBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F0F0F0")
        }
        canvas.drawRect(margin, currentY, pageWidth - margin, currentY + 20, headerBgPaint)

        var x = margin
        for (i in colHeaders.indices) {
            canvas.drawText(colHeaders[i], x + 3, currentY + 14, headerPaint)
            x += colWidths[i]
        }
        currentY += 25

        // Table rows
        for ((index, item) in items.withIndex()) {
            // Check if we need a new page
            if (currentY > pageHeight - 150) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }

            x = margin
            canvas.drawText("${index + 1}", x + 3, currentY, bodyPaint); x += colWidths[0]
            canvas.drawText(item.productName, x + 3, currentY, bodyPaint); x += colWidths[1]
            canvas.drawText("${item.quantity} ${item.unit}", x + 3, currentY, bodyPaint); x += colWidths[2]
            canvas.drawText(CurrencyUtils.formatIndian(item.rate), x + 3, currentY, bodyPaint); x += colWidths[3]
            canvas.drawText(CurrencyUtils.formatIndian(item.taxableAmount), x + 3, currentY, bodyPaint); x += colWidths[4]
            canvas.drawText("${item.gstRate}%", x + 3, currentY, bodyPaint); x += colWidths[5]
            canvas.drawText(CurrencyUtils.formatIndian(item.totalAmount), x + 3, currentY, bodyPaint)

            currentY += 16

            // Row divider
            canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
            currentY += 4
        }

        currentY += 10

        // Totals section - right aligned
        val totalsX = pageWidth - margin - 200

        fun drawTotalRow(label: String, value: String, isBold: Boolean = false) {
            val paint = if (isBold) headerPaint else bodyPaint
            canvas.drawText(label, totalsX, currentY, paint)
            canvas.drawText(value, pageWidth - margin - 80, currentY, paint)
            currentY += 16
        }

        drawTotalRow("Subtotal:", CurrencyUtils.formatIndian(invoice.subtotal))

        if (invoice.totalDiscount > 0) {
            drawTotalRow("Discount:", "-${CurrencyUtils.formatIndian(invoice.totalDiscount)}")
        }

        if (invoice.totalCgst > 0) {
            drawTotalRow("CGST:", CurrencyUtils.formatIndian(invoice.totalCgst))
            drawTotalRow("SGST:", CurrencyUtils.formatIndian(invoice.totalSgst))
        }

        if (invoice.totalIgst > 0) {
            drawTotalRow("IGST:", CurrencyUtils.formatIndian(invoice.totalIgst))
        }

        if (invoice.roundOff != 0.0) {
            drawTotalRow("Round Off:", CurrencyUtils.formatIndian(invoice.roundOff))
        }

        // Grand Total
        canvas.drawLine(totalsX, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 5
        drawTotalRow("Grand Total:", CurrencyUtils.formatIndian(invoice.grandTotal), true)

        currentY += 10

        // Amount in words
        canvas.drawText("Amount in words: ${CurrencyUtils.amountInWords(invoice.grandTotal)}", margin, currentY, smallPaint)
        currentY += 20

        // Bank Details
        if (businessProfile.bankName.isNotEmpty()) {
            canvas.drawText("Bank Details:", margin, currentY, headerPaint)
            currentY += 14
            canvas.drawText("${businessProfile.bankName} | A/c: ${businessProfile.bankAccount} | IFSC: ${businessProfile.bankIfsc}", margin, currentY, bodyPaint)
            currentY += 20
        }

        // Terms
        if (businessProfile.termsAndConditions.isNotEmpty()) {
            canvas.drawText("Terms & Conditions:", margin, currentY, headerPaint)
            currentY += 14
            for (line in businessProfile.termsAndConditions.split("\n")) {
                canvas.drawText(line, margin, currentY, smallPaint)
                currentY += 12
            }
        }

        // Signature
        currentY += 20
        canvas.drawText("For ${businessProfile.businessName}", pageWidth - margin - 120, currentY, bodyPaint)
        currentY += 30
        canvas.drawText("Authorized Signatory", pageWidth - margin - 100, currentY, bodyPaint)

        document.finishPage(page)

        // Save to file
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName ?: "${invoice.invoiceNumber}.pdf")
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.close()

        return file
    }
}
