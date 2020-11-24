/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.mixin.core.util.registry;

import com.google.common.collect.BiMap;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.vanilla.bridge.util.registry.SimpleRegistryBridge;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin_Vanilla implements SimpleRegistryBridge {

    // @formatter:off
    @Shadow @Final @Mutable protected IntIdentityHashBiMap<?> underlyingIntegerMap;
    @Shadow protected Object[] values;
    @Shadow @Final protected BiMap<ResourceLocation, ?> registryObjects;
    // @formatter:on

    @Shadow private int nextFreeId;

    @Override
    public <V> void bridge$remove(final V value) {
        final IntIdentityHashBiMap<Object> map = new IntIdentityHashBiMap<>(this.underlyingIntegerMap.size() - 1);
        for (Object next : this.underlyingIntegerMap) {
            if (next == value) {
                continue;
            }

            map.add(next);
        }

        this.underlyingIntegerMap = map;
        this.registryObjects.inverse().remove(value);

        this.values = null;
        this.nextFreeId = this.underlyingIntegerMap.size();
    }
}
