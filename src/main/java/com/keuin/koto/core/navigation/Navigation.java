package com.keuin.koto.core.navigation;

import net.minecraft.text.MutableText;

public interface Navigation {
    double getDistance();

    double getYaw();

    MutableText getRichText();
}
