package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Set;

public class PvPListener implements Listener {

    private static final Set<Material> DISABLED_ITEMS = Set.of(
            Material.CROSSBOW,
            Material.TRIDENT,
            Material.MACE,
            Material.SHIELD,
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.COPPER_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE,
            Material.WOODEN_SPEAR,
            Material.STONE_SPEAR,
            Material.COPPER_SPEAR,
            Material.IRON_SPEAR,
            Material.GOLDEN_SPEAR,
            Material.DIAMOND_SPEAR,
            Material.NETHERITE_SPEAR
    );

    private final Main plugin;

    public PvPListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        if (event.getDamager() instanceof Player attacker) {
            handlePlayerVsPlayer(event, attacker, defender);
            return;
        }

        if (event.getDamager() instanceof Projectile projectile) {
            handleProjectile(event, projectile, defender);
        }
    }

    private void handlePlayerVsPlayer(EntityDamageByEntityEvent event, Player attacker, Player defender) {
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        Material type = weapon.getType();

        if (isWeaponDisabled(type)) {
            event.setCancelled(true);
            CustomMessage.error(attacker, type.name() + " is disabled in PvP.");
            return;
        }

        if (DISABLED_ITEMS.contains(Material.SHIELD) && defender.isBlocking()) {
            handleShieldBlock(event, attacker, defender);
        }
    }

    private void handleProjectile(EntityDamageByEntityEvent event, Projectile projectile, Player defender) {
        if (!(projectile.getShooter() instanceof Player shooter)) return;

        if (isProjectileDisabled(projectile)) {
            event.setCancelled(true);
            if (projectile instanceof AbstractArrow) {
                projectile.remove();
                CustomMessage.error(shooter, "CROSSBOW is disabled in PvP.");
                return;
            }

            CustomMessage.error(shooter, projectile.getType().name() + " is disabled in PvP.");
            return;
        }

        if (projectile instanceof AbstractArrow) {
            event.setDamage(event.getDamage() * 0.8);
        }

        if (DISABLED_ITEMS.contains(Material.SHIELD) && defender.isBlocking()) {
            handleShieldBlock(event, shooter, defender);

            if (projectile instanceof AbstractArrow arrow) {
                applyArrowEffects(arrow, defender);
                arrow.remove();
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 0.5f);
            }
        }

        if (projectile instanceof AbstractArrow) {
            logTargetHealth(shooter, defender);
        }
    }

    private boolean isWeaponDisabled(Material material) {
        return DISABLED_ITEMS.contains(material);
    }

    private boolean isProjectileDisabled(Projectile projectile) {
        if (projectile instanceof Trident && isWeaponDisabled(Material.TRIDENT)) return true;
        if (projectile instanceof AbstractArrow arrow && isWeaponDisabled(Material.CROSSBOW)) return arrow.isShotFromCrossbow();
        return false;
    }

    private void handleShieldBlock(EntityDamageByEntityEvent event, Player attacker, Player defender) {
        disableShield(defender);
        event.setCancelled(true);
        defender.damage(event.getDamage(), attacker);
    }

    private void applyArrowEffects(AbstractArrow arrow, Player defender) {
        if (arrow instanceof Arrow normalArrow) {
            PotionType baseType = normalArrow.getBasePotionType();
            if (baseType == null) return;

            for (PotionEffect effect : baseType.getPotionEffects()) {
                int adjustedDuration = effect.getDuration() / 8;
                defender.addPotionEffect(new PotionEffect(effect.getType(), adjustedDuration, effect.getAmplifier()));
            }
        }

        else if (arrow instanceof SpectralArrow spectralArrow) {
            defender.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, spectralArrow.getGlowingTicks(), 0));
        }
    }

    private void disableShield(Player player) {
        player.setCooldown(Material.SHIELD, 20);
        player.clearActiveItem();
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 1f);
        CustomMessage.error(player, "SHIELD is disabled in PvP.");
    }

    private void logTargetHealth(Player attacker, Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isDead()) return;

            int halfHearts = (int)(target.getHealth() + target.getAbsorptionAmount())+1;
            NamedTextColor playerColor = plugin.getTeamManager().getColor(target);

            Component msg = Component.text(target.getName(), playerColor)
                    .append(Component.text(" is now at ", NamedTextColor.GRAY))
                    .append(getColoredHealth(halfHearts));

            attacker.sendMessage(msg);
        });
    }

    private Component getColoredHealth(int health) {
        String s = health+"❤";
        NamedTextColor color;

        if (health > 12) color = NamedTextColor.GREEN;
        else if (health > 6) color = NamedTextColor.YELLOW;
        else color = NamedTextColor.RED;

        return Component.text(s, color);
    }
}
