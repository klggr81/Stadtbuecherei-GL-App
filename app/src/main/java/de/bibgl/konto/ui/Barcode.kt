package de.bibgl.konto.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/*
 * Code 39 - so ist die Ausweisnummer auf dem Ausweis der Stadtbuecherei gedruckt
 * (ohne Pruefziffer, Breit zu Schmal etwa 2,5 : 1). Das Verfahren ist einfach
 * genug, dass die App ohne Barcode-Bibliothek auskommt.
 *
 * Jedes Zeichen sind 9 Elemente (Strich, Luecke, Strich ...), davon 3 breit.
 * Bit 8 ist das erste Element, 1 = breit. Werte wie in der ZXing-Bibliothek.
 */
private const val CODE39_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%"
private val CODE39_PATTERNS = intArrayOf(
    0x034, 0x121, 0x061, 0x160, 0x031, 0x130, 0x070, 0x025, 0x124, 0x064, // 0-9
    0x109, 0x049, 0x148, 0x019, 0x118, 0x058, 0x00D, 0x10C, 0x04C, 0x01C, // A-J
    0x103, 0x043, 0x142, 0x013, 0x112, 0x052, 0x007, 0x106, 0x046, 0x016, // K-T
    0x181, 0x0C1, 0x1C0, 0x091, 0x190, 0x0D0, 0x085, 0x184, 0x0C4, 0x0A8, // U-$
    0x0A2, 0x08A, 0x02A, // / + %
)
private const val CODE39_START_STOP = 0x094 // '*'

private const val NARROW = 2
private const val WIDE = 5

/**
 * Breiten der Elemente in Einheiten, beginnend mit einem Strich. Null, wenn
 * [value] Zeichen enthaelt, die Code 39 nicht kennt.
 */
internal fun code39Widths(value: String): List<Int>? {
    if (value.isEmpty()) return null
    val patterns = value.uppercase().map { c ->
        val i = CODE39_ALPHABET.indexOf(c)
        if (i < 0) return null
        CODE39_PATTERNS[i]
    }
    val widths = mutableListOf<Int>()
    (listOf(CODE39_START_STOP) + patterns + CODE39_START_STOP).forEachIndexed { index, pattern ->
        // Schmale Luecke zwischen den Zeichen.
        if (index > 0) widths += NARROW
        for (bit in 8 downTo 0) widths += if ((pattern shr bit) and 1 == 1) WIDE else NARROW
    }
    return widths
}

/**
 * Ausweisnummer als Barcode zum Vorzeigen an der Selbstverbuchung oder Theke.
 * Immer schwarz auf weiss, auch im dunklen Design - Scanner brauchen den
 * Kontrast. Der weisse Rand ist die Ruhezone, die Scanner links und rechts
 * erwarten.
 */
@Composable
fun Code39Barcode(value: String, modifier: Modifier = Modifier) {
    val widths = remember(value) { code39Widths(value) } ?: return
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Barcode der Ausweisnummer $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            // Ganze Pixel je Einheit, damit alle schmalen (und alle breiten)
            // Striche exakt gleich breit sind - unscharfe Kanten mag kein Scanner.
            val total = widths.sum()
            val unit = maxOf(1, (size.width / total).toInt()).toFloat()
            var x = ((size.width - total * unit) / 2).coerceAtLeast(0f).toInt().toFloat()
            widths.forEachIndexed { index, w ->
                if (index % 2 == 0) {
                    drawRect(Color.Black, Offset(x, 0f), Size(w * unit, size.height))
                }
                x += w * unit
            }
        }
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
