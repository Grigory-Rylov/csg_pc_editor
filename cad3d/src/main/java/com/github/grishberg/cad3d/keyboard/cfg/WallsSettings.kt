package com.github.grishberg.cad3d.keyboard.cfg

data class WallsSettings(
    val bottomBorderHeight: Double = 1.0,
    val outerVerticalOffset: Double = 10.0,
    val outerHorizontalOffset: Double = 15.0,
    val outerBorderZOffset: Double = -6.0,

    val borderThickness: Double = 1.5,
    val borderHeight: Double = 4.0,
    val verticalOffset: Double = 5.0,
    val leftOffset: Double = 10.0,
    val rightOffset: Double = 10.0,
    val borderZOffset: Double = -2.0,

    val outerLeftOffset: Double = 15.0,
    val outerRightOffset: Double = 15.0,
    )
