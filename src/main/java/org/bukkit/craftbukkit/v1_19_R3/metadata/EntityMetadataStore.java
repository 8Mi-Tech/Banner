package org.bukkit.craftbukkit.v1_19_R3.metadata;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataStore;
import org.bukkit.metadata.MetadataStoreBase;

/**
 * An EntityMetadataStore stores metadata values for all {@link Entity} classes an their descendants.
 */
public class EntityMetadataStore extends MetadataStoreBase<Entity> implements MetadataStore<Entity> {
    /**
     * Generates a unique metadata key for an {@link Entity} UUID.
     *
     * @see MetadataStoreBase#disambiguate(Object, String)
     * @param entity the entity
     * @param metadataKey The name identifying the metadata value
     * @return a unique metadata key
     */
    @Override
    protected String disambiguate(Entity entity, String metadataKey) {
        return entity.getUniqueId().toString() + ":" + metadataKey;
    }
}
