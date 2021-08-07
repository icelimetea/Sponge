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
package org.spongepowered.common.mixin.inventory.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;

@Mixin(value = ServerPlayer.class)
public class ServerPlayerEntityMixin_Inventory extends PlayerEntityMixin_Inventory {

    @Redirect(method = "openMenu",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;addSlotListener(Lnet/minecraft/world/inventory/ContainerListener;)V"))
    private void impl$onOpenMenu(final AbstractContainerMenu abstractContainerMenu, final ContainerListener param0, final @Nullable MenuProvider containerProvider) {
        ((TrackedContainerBridge) abstractContainerMenu).bridge$trackViewable(containerProvider);
        try (InventoryPacketContext context = PacketPhase.Inventory.OPEN_INVENTORY.createPhaseContext(PhaseTracker.SERVER)) {
            context.buildAndSwitch();
            abstractContainerMenu.addSlotListener(param0);
        }
    }

    @Inject(method = "openHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;addSlotListener(Lnet/minecraft/world/inventory/ContainerListener;)V"))
    private void impl$onOpenHorseInventory(final AbstractHorse horse, final Container inventoryIn, final CallbackInfo ci) {
        ((TrackedContainerBridge) this.containerMenu).bridge$trackViewable(inventoryIn);
    }

    @Redirect(method = "doCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;removed(Lnet/minecraft/world/entity/player/Player;)V"))
    private void impl$onCloseContainer(final AbstractContainerMenu abstractContainerMenu, final Player param0) {
        abstractContainerMenu.removed(param0);
        ((TrackedContainerBridge) abstractContainerMenu).bridge$detectAndSendChanges(true);
    }

}
