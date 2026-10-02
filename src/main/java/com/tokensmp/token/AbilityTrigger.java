package com.tokensmp.token;

/**
 * How an ability is activated. Every ability declares its own trigger, so
 * different tiers of the same token can use different keybinds.
 *
 * - SHIFT_RIGHT_CLICK: the classic token keybind (sneak + right click).
 * - RIGHT_CLICK:       a plain right click with the token item held.
 * - SHIFT_LEFT_CLICK:  sneak + left click.
 */
public enum AbilityTrigger {

    SHIFT_RIGHT_CLICK,
    RIGHT_CLICK,
    SHIFT_LEFT_CLICK
}
