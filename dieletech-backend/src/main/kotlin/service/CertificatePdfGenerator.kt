package service

import com.dieletech.backend.model.Certificate
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream

/**
 * Dibuja el certificado en A4 apaisado con PDFBox.
 * Usa unicamente fuentes base de PDF (Helvetica y Times), asi que no
 * depende de ningun archivo de fuente en el servidor.
 */
@Component
class CertificatePdfGenerator {

    private val brand = Color(29, 78, 216)     // #1d4ed8
    private val ink = Color(16, 26, 40)        // #101a28
    private val ink2 = Color(64, 80, 106)      // #40506a
    private val ink3 = Color(107, 124, 150)    // #6b7c96
    private val teal = Color(15, 118, 110)     // #0f766e
    private val line = Color(213, 220, 230)    // #d5dce6

    fun render(cert: Certificate, verifyUrl: String, issuedAtText: String): ByteArray {
        PDDocument().use { doc ->
            val page = PDPage(PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width)) // apaisado
            doc.addPage(page)

            val W = page.mediaBox.width
            val H = page.mediaBox.height

            PDPageContentStream(doc, page).use { c ->

                // ── Marco ──
                c.setNonStrokingColor(Color.WHITE)
                c.addRect(0f, 0f, W, H); c.fill()

                c.setNonStrokingColor(brand)
                c.addRect(0f, H - 14f, W, 14f); c.fill()
                c.addRect(0f, 0f, W, 6f); c.fill()

                c.setStrokingColor(line)
                c.setLineWidth(0.8f)
                c.addRect(28f, 22f, W - 56f, H - 56f); c.stroke()

                // ── Encabezado ──
                text(c, PDType1Font.HELVETICA_BOLD, 13f, brand, 56f, H - 62f, "DIELETECH")
                text(c, PDType1Font.HELVETICA, 8.5f, ink3, 56f, H - 76f, "Tu ruta hacia tu 1o en Tech")

                val issuer = "CERTIFICADO DE FINALIZACION"
                val iw = PDType1Font.HELVETICA_BOLD.getStringWidth(issuer) / 1000 * 8.5f
                text(c, PDType1Font.HELVETICA_BOLD, 8.5f, ink3, W - 56f - iw, H - 62f, issuer)

                // ── Cuerpo ──
                var y = H - 140f
                centered(c, PDType1Font.TIMES_BOLD, 34f, ink, W, y, "Certificado")
                y -= 26f
                centered(c, PDType1Font.HELVETICA, 11f, ink2, W, y, "Se certifica que")

                y -= 44f
                val name = cert.studentName.uppercase()
                val nameSize = fitSize(PDType1Font.TIMES_BOLD, name, W - 200f, 30f)
                centered(c, PDType1Font.TIMES_BOLD, nameSize, brand, W, y, name)

                // Subrayado del nombre
                val nw = PDType1Font.TIMES_BOLD.getStringWidth(name) / 1000 * nameSize
                c.setStrokingColor(line); c.setLineWidth(0.7f)
                c.moveTo((W - nw) / 2 - 24f, y - 12f)
                c.lineTo((W + nw) / 2 + 24f, y - 12f)
                c.stroke()

                y -= 44f
                centered(c, PDType1Font.HELVETICA, 11f, ink2, W, y,
                    "completó satisfactoriamente el curso y aprobó su evaluación final")

                y -= 34f
                val title = cert.courseTitle
                val titleSize = fitSize(PDType1Font.HELVETICA_BOLD, title, W - 200f, 20f)
                centered(c, PDType1Font.HELVETICA_BOLD, titleSize, ink, W, y, title)

                y -= 24f
                val detail = "${cert.courseHours} horas de contenido" +
                    "   ·   Calificación obtenida ${cert.score}%" +
                    (cert.instructorName?.let { "   ·   Instructor $it" } ?: "")
                centered(c, PDType1Font.HELVETICA, 9.5f, ink3, W, y, detail)

                // ── Pie: firma, fecha y verificacion ──
                val footY = 92f

                c.setStrokingColor(line); c.setLineWidth(0.7f)
                c.moveTo(72f, footY + 26f); c.lineTo(242f, footY + 26f); c.stroke()
                text(c, PDType1Font.HELVETICA_BOLD, 9f, ink, 72f, footY + 12f, "Equipo Dieletech")
                text(c, PDType1Font.HELVETICA, 8f, ink3, 72f, footY, "Dirección académica")

                c.moveTo(282f, footY + 26f); c.lineTo(452f, footY + 26f); c.stroke()
                text(c, PDType1Font.HELVETICA_BOLD, 9f, ink, 282f, footY + 12f, issuedAtText)
                text(c, PDType1Font.HELVETICA, 8f, ink3, 282f, footY, "Fecha de emisión")

                // Codigo de verificacion
                text(c, PDType1Font.HELVETICA, 8f, ink3, 510f, footY + 34f, "CÓDIGO DE VERIFICACIÓN")
                text(c, PDType1Font.COURIER_BOLD, 13f, teal, 510f, footY + 16f, cert.code)
                text(c, PDType1Font.HELVETICA, 7.5f, ink3, 510f, footY + 2f, verifyUrl)

                // QR
                runCatching {
                    val qr = qrImage(verifyUrl, 240)
                    val img = LosslessFactory.createFromImage(doc, qr)
                    c.drawImage(img, W - 132f, footY - 6f, 74f, 74f)
                    text(c, PDType1Font.HELVETICA, 6.5f, ink3, W - 132f, footY - 16f, "Escanea para verificar")
                }
            }

            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        }
    }

    // ── Utilidades de dibujo ──

    private fun text(
        c: PDPageContentStream, font: PDFont, size: Float,
        color: Color, x: Float, y: Float, value: String
    ) {
        c.beginText()
        c.setFont(font, size)
        c.setNonStrokingColor(color)
        c.newLineAtOffset(x, y)
        c.showText(sanitize(value))
        c.endText()
    }

    private fun centered(
        c: PDPageContentStream, font: PDFont, size: Float,
        color: Color, pageWidth: Float, y: Float, value: String
    ) {
        val clean = sanitize(value)
        val w = font.getStringWidth(clean) / 1000 * size
        text(c, font, size, color, (pageWidth - w) / 2, y, clean)
    }

    /** Reduce el tamano hasta que el texto quepa en el ancho disponible. */
    private fun fitSize(font: PDFont, value: String, maxWidth: Float, start: Float): Float {
        var size = start
        val clean = sanitize(value)
        while (size > 10f && font.getStringWidth(clean) / 1000 * size > maxWidth) size -= 0.5f
        return size
    }

    /**
     * Las fuentes base de PDF usan WinAnsi: cualquier caracter fuera de ese
     * juego rompe showText. Se sustituyen en lugar de fallar.
     */
    private fun sanitize(value: String): String = buildString {
        for (ch in value) {
            when {
                ch.code in 32..126 -> append(ch)
                ch in "áéíóúüñÁÉÍÓÚÜÑ°ºª¿¡·" -> append(ch)
                ch == '’' || ch == '‘' -> append('\'')
                ch == '“' || ch == '”' -> append('"')
                ch == '—' || ch == '–' -> append('-')
                else -> append(' ')
            }
        }
    }

    private fun qrImage(content: String, size: Int): java.awt.image.BufferedImage {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        return MatrixToImageWriter.toBufferedImage(matrix)
    }
}
