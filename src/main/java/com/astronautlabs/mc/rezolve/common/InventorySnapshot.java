package com.astronautlabs.mc.rezolve.common;

import net.minecraft.item.ItemStack;

public class InventorySnapshot {
	public InventorySnapshot(ItemStack[] slots) {
		this.slots = slots;
	}
	
	private ItemStack[] slots;
	
	public ItemStack[] getSlots() {
		return this.slots;
	}
}
