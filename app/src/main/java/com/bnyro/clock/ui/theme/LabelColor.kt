package com.bnyro.clock.ui.theme

import androidx.annotation.StringRes
import com.bnyro.clock.R

const val SnowLabelColor: Int = -1

enum class LabelColor(@param:StringRes val label: Int, val argb: Int) {
    Snow(R.string.label_color_snow, SnowLabelColor),
    Charcoal(R.string.label_color_charcoal, 0xFF000000.toInt()),
    Radicchio(R.string.label_color_radicchio, 0xFFB55375.toInt()),
    CherryBlossom(R.string.label_color_cherry_blossom, 0xFFD65C7F.toInt()),
    Flamingo(R.string.label_color_flamingo, 0xFFD98580.toInt()),
    Tomato(R.string.label_color_tomato, 0xFFE65C43.toInt()),
    Tangerine(R.string.label_color_tangerine, 0xFFED754A.toInt()),
    Pumpkin(R.string.label_color_pumpkin, 0xFFDE843D.toInt()),
    Mango(R.string.label_color_mango, 0xFFE9A544.toInt()),
    Banana(R.string.label_color_banana, 0xFFF0CB57.toInt()),
    Citron(R.string.label_color_citron, 0xFFDCCB60.toInt()),
    Avocado(R.string.label_color_avocado, 0xFFB9C35B.toInt()),
    Pistachio(R.string.label_color_pistachio, 0xFF86AB5C.toInt()),
    Basil(R.string.label_color_basil, 0xFF509469.toInt()),
    Sage(R.string.label_color_sage, 0xFF63B28A.toInt()),
    Eucalyptus(R.string.label_color_eucalyptus, 0xFF52A69A.toInt()),
    Peacock(R.string.label_color_peacock, 0xFF5EA7CF.toInt()),
    Cobalt(R.string.label_color_cobalt, 0xFF7399DF.toInt()),
    Lavender(R.string.label_color_lavender, 0xFF979BD9.toInt()),
    Blueberry(R.string.label_color_blueberry, 0xFF7C7BCD.toInt()),
    Wisteria(R.string.label_color_wisteria, 0xFFAF99C8.toInt()),
    Amethyst(R.string.label_color_amethyst, 0xFFAC81B8.toInt()),
    Grape(R.string.label_color_grape, 0xFFB566C3.toInt()),
    Cocoa(R.string.label_color_cocoa, 0xFF9C7D75.toInt()),
    Graphite(R.string.label_color_graphite, 0xFF888888.toInt()),
    Birch(R.string.label_color_birch, 0xFFA79E8D.toInt())
}
