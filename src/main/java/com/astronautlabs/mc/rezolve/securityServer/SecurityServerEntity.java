package com.astronautlabs.mc.rezolve.securityServer;

import java.util.ArrayList;
import java.util.UUID;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.securityServer.SecurityServerEntity.Rule;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class SecurityServerEntity extends MachineEntity {

	public SecurityServerEntity() {
		super("security_server_tile_entity");
		this.rules = new ArrayList<Rule>();
		this.rules.add(new Rule("<machines>", Rule.MODE_OPEN));
		this.rules.add(new Rule("<players>", Rule.MODE_RESTRICTED));
		this.rules.add(new Rule("rezonaut", Rule.MODE_OWNER));
		this.rules.add(new Rule("maxillaria", Rule.MODE_OWNER));
		this.rules.add(new Rule("dudeguypal", Rule.MODE_OWNER));
		this.rules.add(new Rule("hypergamer", Rule.MODE_OWNER));
	}

	public class Rule {
		public Rule(String id, String name, int mode) {
			this.id = id;
			this.name = name;
			this.mode = mode;
		}

		public Rule(String name, int mode) {
			this.id = UUID.randomUUID().toString();
			this.name = name;
			this.mode = mode;
		}

		public static final int MODE_OPEN = -1;
		public static final int MODE_RESTRICTED = 0;
		public static final int MODE_ALLOWED = 1;
		public static final int MODE_OWNER = 2;

		public static final int MODE_NONE = 0;
		public static final int MODE_PROTECTED = 1;

		String id;
		String name;
		int mode;
		
		public String getId() {
			return this.id;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setMode(int mode) {
			this.mode = mode;
		}
		
		public int getMode() {
			return this.mode;
		}
		
		public Rule copy() {
			return new Rule(this.id, this.name, this.mode);
		}
	}
	
	ArrayList<Rule> rules = new ArrayList<Rule>();
	String rootUser = null;
	
	public void addRule(String name, int mode) {
		if (this.getWorld().isRemote) {
			
			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
				Minecraft.getMinecraft().thePlayer, 
				this, "", name, mode
			));
			
			return;
		}
		
		this.rules.add(new Rule(name, mode));
		this.notifyUpdate();
	}

	public Rule getRuleById(String id) {
		for (Rule rule : this.rules) {
			if (id.equals(rule.id))
				return rule.copy();
		}
		
		return null;
	}

	private Rule getRuleByIdInternal(String id) {
		for (Rule rule : this.rules) {
			if (id.equals(rule.id))
				return rule;
		}
		
		return null;
	}
	
	public boolean editRule(String id, String name, int mode) {
		Rule rule = this.getRuleByIdInternal(id);
		
		if (rule == null)
			return false;
		
		if (this.getWorld().isRemote) {
			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
				Minecraft.getMinecraft().thePlayer, 
				this, id, name, mode
			));
				
			return true;
		}

		if (!"<players>".equals(rule.getName()) && !"<machines>".equals(rule.getName())) {
			rule.name = name;
		}
		
		rule.mode = mode;
		this.notifyUpdate();
		
		return true;
	}
	
	public Rule[] getRules() {
		ArrayList<Rule> ruleCopies = new ArrayList<Rule>();
		for (Rule rule : this.rules)
			ruleCopies.add(rule.copy());
		
		return ruleCopies.toArray(new Rule[this.rules.size()]);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		if (compound.hasKey("RootUser")) {
			this.rootUser = compound.getString("RootUser");
		}
		
		if (compound.hasKey("Rules")) {
			NBTTagList rulesList = compound.getTagList("Rules", NBT.TAG_COMPOUND);
			this.rules = new ArrayList<Rule>();
			
			for (int i = 0, max = rulesList.tagCount(); i < max; ++i) {
				NBTTagCompound ruleNBT = (NBTTagCompound)rulesList.get(i);
				this.rules.add(new Rule(ruleNBT.getString("ID"), ruleNBT.getString("Name"), ruleNBT.getInteger("Mode")));
			}
		}
		
		if (this.rules == null || this.rules.size() == 0) {
			// Reset rules.
			this.rules = new ArrayList<Rule>();
			this.rules.add(new Rule("<machines>", Rule.MODE_OPEN));
			this.rules.add(new Rule("<players>", Rule.MODE_RESTRICTED));
		}
		
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		if (this.rootUser != null) {
			compound.setString("RootUser", this.rootUser);
		}
		
		NBTTagList rulesList = new NBTTagList();
		
		for (Rule rule : this.rules) {
			NBTTagCompound ruleNBT = new NBTTagCompound();
			ruleNBT.setString("ID", rule.id);
			ruleNBT.setString("Name", rule.name);
			ruleNBT.setInteger("Mode", rule.mode);
			rulesList.appendTag(ruleNBT);
		}
		
		compound.setTag("Rules", rulesList);
		return super.writeToNBT(compound);
	}

	public void removeRule(String id) {
		this.removeRule(this.getRuleById(id));
	}
	
	public void removeRule(Rule selectedRule) {
		if (selectedRule == null)
			return;
		
		if (this.getWorld().isRemote) {

			RezolvePacketHandler.INSTANCE.sendToServer(new RuleModificationMessage(
				Minecraft.getMinecraft().thePlayer, 
				this, selectedRule.getId(), "", -2
			));
			
			return;
		}
		
		Rule ruleToRemove = this.getRuleByIdInternal(selectedRule.getId());
		if (ruleToRemove == null)
			return;

		if ("<players>".equals(ruleToRemove.getName()))
			return;
		if ("<machines>".equals(ruleToRemove.getName()))
			return;
		
		this.rules.remove(ruleToRemove);
		this.notifyUpdate();
	}

	public Rule getRuleByName(String name) {
		for (Rule rule : this.rules) {
			if (name.equals(rule.name))
				return rule.copy();
		}
		
		return null;
	}
}
