package com.example.util

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object IconShapeHelper {

    val SHAPES = listOf(
        "Circle",
        "Squircle",
        "Rounded Square",
        "Smooth Square",
        "Teardrop",
        "Teardrop Right",
        "Teardrop Inverted",
        "Pebble",
        "Leaf",
        "Reverse Leaf",
        "Hexagon",
        "Octagon",
        "Pentagon",
        "Heptagon",
        "Decagon",
        "Diamond",
        "Star",
        "Compass Star",
        "Flower",
        "Badge",
        "Shield",
        "Heart",
        "Clover",
        "Cut Corner",
        "Diagonal Cut",
        "Opposite Cut",
        "Arch",
        "Bowl",
        "Pill",
        "Oval",
        "Ticket",
        "Square"
    )

    fun getShape(name: String): Shape {
        return when (name) {
            "Circle" -> CircleShape
            "Squircle" -> SquircleShape
            "Rounded Square" -> RoundedCornerShape(26)
            "Smooth Square" -> RoundedCornerShape(16)
            "Teardrop" -> RoundedCornerShape(topStart = 0.dp, topEnd = 30.dp, bottomEnd = 30.dp, bottomStart = 30.dp)
            "Teardrop Right" -> RoundedCornerShape(topStart = 30.dp, topEnd = 0.dp, bottomEnd = 30.dp, bottomStart = 30.dp)
            "Teardrop Inverted" -> RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 30.dp, bottomStart = 0.dp)
            "Pebble" -> RoundedCornerShape(topStart = 32.dp, topEnd = 16.dp, bottomEnd = 32.dp, bottomStart = 16.dp)
            "Leaf" -> RoundedCornerShape(topStart = 32.dp, topEnd = 6.dp, bottomEnd = 32.dp, bottomStart = 6.dp)
            "Reverse Leaf" -> RoundedCornerShape(topStart = 6.dp, topEnd = 32.dp, bottomEnd = 6.dp, bottomStart = 32.dp)
            "Hexagon" -> HexagonShape
            "Octagon" -> OctagonShape
            "Pentagon" -> PentagonShape
            "Heptagon" -> HeptagonShape
            "Decagon" -> DecagonShape
            "Diamond" -> DiamondShape
            "Star" -> Star5Shape
            "Compass Star" -> Star8Shape
            "Flower" -> FlowerShape
            "Badge" -> BadgeScallopShape
            "Shield" -> ShieldShape
            "Heart" -> HeartShape
            "Clover" -> CloverShape
            "Cut Corner" -> CutCornerShape(12.dp)
            "Diagonal Cut" -> CutCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 16.dp)
            "Opposite Cut" -> CutCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 0.dp)
            "Arch" -> RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp, bottomEnd = 8.dp, bottomStart = 8.dp)
            "Bowl" -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 50.dp, bottomStart = 50.dp)
            "Pill" -> RoundedCornerShape(50)
            "Oval" -> OvalShape
            "Ticket" -> TicketShape
            "Square" -> RoundedCornerShape(4.dp)
            else -> CircleShape
        }
    }

    val SquircleShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val r = w * 0.22f
        moveTo(0f, r)
        cubicTo(0f, 0f, 0f, 0f, r, 0f)
        lineTo(w - r, 0f)
        cubicTo(w, 0f, w, 0f, w, r)
        lineTo(w, h - r)
        cubicTo(w, h, w, h, w - r, h)
        lineTo(r, h)
        cubicTo(0f, h, 0f, h, 0f, h - r)
        close()
    }

    val HexagonShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.98f
        for (i in 0 until 6) {
            val angle = (i * 60.0 - 30.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val OctagonShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.98f
        for (i in 0 until 8) {
            val angle = (i * 45.0 - 22.5) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val PentagonShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + (h * 0.04f)
        val r = (w.coerceAtMost(h) / 2f) * 0.96f
        for (i in 0 until 5) {
            val angle = (i * 72.0 - 90.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val HeptagonShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.98f
        for (i in 0 until 7) {
            val angle = (i * (360.0 / 7.0) - 90.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val DecagonShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.98f
        for (i in 0 until 10) {
            val angle = (i * 36.0 - 18.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val DiamondShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val r = 12f
        moveTo(w / 2f, r)
        lineTo(w - r, h / 2f)
        lineTo(w / 2f, h - r)
        lineTo(r, h / 2f)
        close()
    }

    val Star5Shape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + (h * 0.03f)
        val outerR = (w.coerceAtMost(h) / 2f) * 0.98f
        val innerR = outerR * 0.54f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * 36.0 - 90.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val Star8Shape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = (w.coerceAtMost(h) / 2f) * 0.98f
        val innerR = outerR * 0.70f
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * 22.5 - 90.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val FlowerShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = (w.coerceAtMost(h) / 2f) * 0.98f
        val innerR = outerR * 0.78f
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * 22.5) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val BadgeScallopShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = (w.coerceAtMost(h) / 2f) * 0.98f
        val innerR = outerR * 0.86f
        for (i in 0 until 24) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * 15.0) * Math.PI / 180.0
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    val ShieldShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.12f, h * 0.05f)
        lineTo(w * 0.88f, h * 0.05f)
        cubicTo(w * 0.96f, h * 0.05f, w * 0.98f, h * 0.40f, w * 0.88f, h * 0.65f)
        lineTo(w * 0.50f, h * 0.98f)
        lineTo(w * 0.12f, h * 0.65f)
        cubicTo(w * 0.02f, h * 0.40f, w * 0.04f, h * 0.05f, w * 0.12f, h * 0.05f)
        close()
    }

    val HeartShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w / 2f, h * 0.88f)
        cubicTo(w * 0.10f, h * 0.62f, 0f, h * 0.35f, 0f, h * 0.22f)
        cubicTo(0f, h * 0.05f, w * 0.25f, 0f, w / 2f, h * 0.20f)
        cubicTo(w * 0.75f, 0f, w, h * 0.05f, w, h * 0.22f)
        cubicTo(w, h * 0.35f, w * 0.90f, h * 0.62f, w / 2f, h * 0.88f)
        close()
    }

    val CloverShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val lobeR = w * 0.28f
        val d = w * 0.20f
        // Top lobe
        addOval(Rect(cx - lobeR, cy - d - lobeR, cx + lobeR, cy - d + lobeR))
        // Bottom lobe
        addOval(Rect(cx - lobeR, cy + d - lobeR, cx + lobeR, cy + d + lobeR))
        // Left lobe
        addOval(Rect(cx - d - lobeR, cy - lobeR, cx - d + lobeR, cy + lobeR))
        // Right lobe
        addOval(Rect(cx + d - lobeR, cy - lobeR, cx + d + lobeR, cy + lobeR))
    }

    val TicketShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val cut = w * 0.16f
        moveTo(cut, 0f)
        lineTo(w - cut, 0f)
        arcTo(Rect(w - cut, -cut, w + cut, cut), 180f, -90f, false)
        lineTo(w, h - cut)
        arcTo(Rect(w - cut, h - cut, w + cut, h + cut), 270f, -90f, false)
        lineTo(cut, h)
        arcTo(Rect(-cut, h - cut, cut, h + cut), 0f, -90f, false)
        lineTo(0f, cut)
        arcTo(Rect(-cut, -cut, cut, cut), 90f, -90f, false)
        close()
    }

    val OvalShape = GenericShape { size, _ ->
        addOval(Rect(0f, 0f, size.width, size.height))
    }
}
