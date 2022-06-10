package com.nokhoon.couchstairs;

import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PluginMain extends JavaPlugin implements Listener {
	private BukkitTask task = null;
	
	@Override
	public void onEnable() {
		task = getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				for(Player player : getServer().getOnlinePlayers()) {
					if(player.isInsideVehicle()) {
						Entity vehicle = player.getVehicle();
						if(vehicle.getType() == EntityType.ARROW) vehicle.setTicksLived(1);
					}
				}
			}
		}, 1111L, 1111L);
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		task.cancel();
		task = null;
	}
	
	@EventHandler
	public void sitOnStairs(PlayerInteractEvent event) {
		if(!event.hasBlock()) return;
		Player player = event.getPlayer();
		if(player.isInsideVehicle() || player.isSneaking() || !((Entity) player).isOnGround()) return;
		GameMode gamemode = player.getGameMode();
		if(gamemode != GameMode.SURVIVAL && gamemode != GameMode.CREATIVE) return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(event.getItem() != null) return;
		if(event.getBlockFace() == BlockFace.DOWN) return;
		Block block = event.getClickedBlock();
		if(!Tag.STAIRS.isTagged(block.getType())) return;
		Stairs stairs = (Stairs) block.getBlockData();
		if(stairs.getShape() != Shape.STRAIGHT || stairs.getHalf() == Half.TOP || stairs.isWaterlogged()) return;
		if(!block.getRelative(BlockFace.UP).isPassable() || !block.getRelative(BlockFace.UP, 2).isPassable()) return;
		Vector direction = stairs.getFacing().getDirection().multiply(0.3);
		direction.setY(0.5);
		var blockCenter = block.getLocation().toCenterLocation();
		if(blockCenter.getWorld().getNearbyEntitiesByType(Arrow.class, blockCenter, 0.5).isEmpty()) {
			event.setCancelled(true);
			Arrow arrow = player.getWorld().spawnArrow(blockCenter.subtract(direction), direction, 0F, 0F);
			arrow.setPickupStatus(PickupStatus.DISALLOWED);
			arrow.getLocation().setDirection(direction);
			arrow.addPassenger(player);
		}
	}
	
	@EventHandler
	public void despawnArrow(EntityDismountEvent event) {
		Entity vehicle = event.getDismounted();
		if(vehicle.getType() == EntityType.ARROW) vehicle.remove();
	}
}
