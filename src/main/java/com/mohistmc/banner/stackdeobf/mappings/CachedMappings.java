package com.mohistmc.banner.stackdeobf.mappings;

// Created by booky10 in StackDeobfuscator (17:04 20.03.23)

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.stackdeobf.mappings.providers.AbstractMappingProvider;
import com.mohistmc.banner.stackdeobf.util.CompatUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CachedMappings {

    // "CLASSES" name has package prefixed (separated by '.')
    private static final Int2ObjectMap<String> CLASSES = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static final Int2ObjectMap<String> METHODS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private static final Int2ObjectMap<String> FIELDS = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    private CachedMappings() {
    }

    public static void init(AbstractMappingProvider provider) {
        CompatUtil.LOGGER.info(BannerMCStart.I18N.get("stackdeobf.creating"));
        ExecutorService cacheExecutor = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat("Mappings Cache Thread").setDaemon(true).build());
        long start = System.currentTimeMillis();

        // visitor expects mappings to be intermediary -> named
        provider.cacheMappings(new MappingCacheVisitor(CLASSES, METHODS, FIELDS), cacheExecutor)
                .thenAccept($ -> {
                    long timeDiff = System.currentTimeMillis() - start;
                    CompatUtil.LOGGER.info(BannerMCStart.I18N.get("stackdeobf.cached.mappings"), timeDiff);

                    CompatUtil.LOGGER.info(" " + BannerMCStart.I18N.get("stackdeobf.classes") + " " + CLASSES.size());
                    CompatUtil.LOGGER.info(" " + BannerMCStart.I18N.get("stackdeobf.methods") + " " + METHODS.size());
                    CompatUtil.LOGGER.info(" " + BannerMCStart.I18N.get("stackdeobf.fields") + " "  + FIELDS.size());
                })
                // needs to be executed asynchronously, otherwise the
                // executor of the current thread would be shut down
                .thenRunAsync(() -> {
                    CompatUtil.LOGGER.info(BannerMCStart.I18N.get("stackdeobf.shutting.down"));
                    BannerMCStart.LOGGER.info(BannerMCStart.I18N.get("load.libraries"));
                    cacheExecutor.shutdown();
                });
    }

    public static @Nullable String remapClass(int id) {
        return CLASSES.get(id);
    }

    public static @Nullable String remapMethod(int id) {
        return METHODS.get(id);
    }

    public static @Nullable String remapField(int id) {
        return FIELDS.get(id);
    }
}
