package com.astronautlabs.mc.rezolve.bundleBuilder;

import java.util.ArrayList;
import java.util.List;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.MetaItemBase;
import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class BundlePatternItem extends MetaItemBase implements ITooltipHint {
	public BundlePatternItem() {
		super("item_bundle_pattern");
	}

	@Override
	public void registerRecipes() {

		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {

			RezolveMod.addRecipe(
				this.blank(), 
				"PSP",
				"GFG",
				"PSP", 
				
				'P', "item|enderio:itemAlloy|5",
				'S', "item|enderio:itemMaterial",
				'G', Items.GLOWSTONE_DUST,
				'F', "item|enderio:itemBasicFilterUpgrade"
			);
			
		} else {
			RezolveMod.addRecipe(
				this.blank(), 
				"OSO",
				"GIG",
				"OSO", 
				
				'O', Blocks.OBSIDIAN,
				'S', Items.SLIME_BALL,
				'G', Items.GLOWSTONE_DUST,
				'I', Items.ITEM_FRAME
			);
		}
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			String localizedName = super.getItemStackDisplayName(stack);
			
			ArrayList<String> props = new ArrayList<String>();
			
			if (nbt.hasKey("Name")) {
				props.add(nbt.getString("Name"));
			}
			
			if (nbt.hasKey("Color")) {
				int dye = nbt.getInteger("Color");
				String colorName = RezolveMod.instance().getColorName(dye);
				props.add(colorName);
			}
			
			if (props.size() > 0)
				return localizedName + " ("+String.join(", ", props)+")";
			else
				return localizedName + " (Configured)";
		}
		
		return super.getItemStackDisplayName(stack);
	}

	private VirtualInventory dummyInventory = new VirtualInventory();
	
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return RezolveMod.BUNDLE_ITEM.describeContents(itemStack);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));
	}

	public boolean isBlank(ItemStack stack) {
		return stack.getMetadata() == 1;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() == 1)
			return this.getUnlocalizedName()+"_blank";
		else
			return this.getUnlocalizedName();
	}

	public ItemStack blank() {
		return this.blank(1);
	}
	
	public ItemStack blank(int size) {
		return new ItemStack(this, size, 1);
	}
}
