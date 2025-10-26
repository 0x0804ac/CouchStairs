package com.nokhoon.couchstairs;

import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PluginMain extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void sitOnStairs(PlayerInteractEvent event) {
		if(!event.hasBlock()) return;
		Player player = event.getPlayer();
		if(player.isInsideVehicle() || player.isSneaking() || !((Entity) player).isOnGround()) return;
		GameMode gamemode = player.getGameMode();
		if(gamemode != GameMode.SURVIVAL && gamemode != GameMode.CREATIVE) return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(EquipmentSlot.HAND != event.getHand() || event.getItem() != null) return;
		if(event.getBlockFace() == BlockFace.DOWN) return;
		Block block = event.getClickedBlock();
		if(!Tag.STAIRS.isTagged(block.getType())) return;
		Stairs stairs = (Stairs) block.getBlockData();
		if(stairs.getShape() != Shape.STRAIGHT || stairs.getHalf() == Half.TOP || stairs.isWaterlogged()) return;
		if(!block.getRelative(BlockFace.UP).isPassable() || !block.getRelative(BlockFace.UP, 2).isPassable()) return;
		Vector direction = stairs.getFacing().getDirection().multiply(0.3);
		direction.setY(0.4);
		var blockCenter = block.getLocation().toCenterLocation();
		if(blockCenter.getWorld().getNearbyEntitiesByType(TextDisplay.class, blockCenter, 0.5).isEmpty()) {
			event.setCancelled(true);
			Entity mount = player.getWorld().spawnEntity(blockCenter.subtract(direction), EntityType.TEXT_DISPLAY, SpawnReason.CUSTOM);
			mount.addPassenger(player);
		}
	}
	
	@EventHandler
	public void despawnArrow(EntityDismountEvent event) {
		Entity vehicle = event.getDismounted();
		if(vehicle.getType() == EntityType.TEXT_DISPLAY && vehicle.getEntitySpawnReason() == SpawnReason.CUSTOM) vehicle.remove();
	}
}
