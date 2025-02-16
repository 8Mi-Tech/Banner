package org.bukkit.craftbukkit.v1_19_R3.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

public abstract class CraftProjectile extends AbstractProjectile implements Projectile {
    public CraftProjectile(CraftServer server, net.minecraft.world.entity.projectile.Projectile entity) {
        super(server, entity);
    }

    @Override
    public ProjectileSource getShooter() {
        return getHandle().bridge$projectileSource();
    }

    @Override
    public void setShooter(ProjectileSource shooter) {
        if (shooter instanceof CraftLivingEntity) {
            getHandle().setOwner((LivingEntity) ((CraftLivingEntity) shooter).entity);
        } else {
            getHandle().setOwner(null);
        }
        getHandle().banner$setProjectileSource(shooter);
    }

    @Override
    public net.minecraft.world.entity.projectile.Projectile getHandle() {
        return (net.minecraft.world.entity.projectile.Projectile) entity;
    }

    @Override
    public String toString() {
        return "CraftProjectile";
    }
}
