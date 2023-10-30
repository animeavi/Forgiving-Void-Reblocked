package me.polonium.forgivingvoidreblocked;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public final class ForgivingVoidReBlocked extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Registering the listener
        getServer().getPluginManager().registerEvents(this, this);
        // Load configurations and save default if missing
        config = getConfig();
        config.addDefault("teleportYLevel", 300);
        config.addDefault("teleportOnlyPlayers", true);
        config.addDefault("allowedWorlds", new ArrayList<>(Arrays.asList("world", "world_nether", "world_the_end")));
        config.addDefault("applyEffects", true);
        config.addDefault("effectDuration", 20);

        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != DamageCause.VOID) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof Player) && !(entity instanceof org.bukkit.entity.Mob)) {
            return;
        }

        if (config.getBoolean("teleportOnlyPlayers") && !(entity instanceof Player)) {
            return;
        }

        if (!isEntityInAllowedWorld(entity.getWorld())) {
            return;
        }

        if (event.getDamage() >= 1000) {
            return;
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (isElytraFlying(player)) {
                return;
            }
        }

        teleportEntity(entity);
        applyEffects(entity);
        event.setCancelled(true);
    }

    private boolean isElytraFlying(Player player) {
        return player.isGliding();
    }

    private boolean isEntityInAllowedWorld(World world) {
        return config.getStringList("allowedWorlds").contains(world.getName());
    }

    private void teleportEntity(Entity entity) {
        Location entitylocation = entity.getLocation();
        entitylocation.setY(getConfig().getInt("teleportYLevel"));
        entity.teleport(entitylocation);

    }

    private void applyEffects(Entity entity) {
        if (!config.getBoolean("applyEffects")) {
            return;
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;
            int tickrate = 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, config.getInt("effectDuration") * tickrate, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, config.getInt("effectDuration") * tickrate, 1));
        }
    }
}
