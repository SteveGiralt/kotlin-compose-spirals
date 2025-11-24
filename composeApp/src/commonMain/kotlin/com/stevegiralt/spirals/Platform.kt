package com.stevegiralt.spirals

/**
 * Whether automatic rotation of the spiral is allowed to better fit the screen.
 * Enabled on Android (fixed screen orientation), disabled on Desktop (resizable windows).
 */
expect val allowSpiralRotation: Boolean
