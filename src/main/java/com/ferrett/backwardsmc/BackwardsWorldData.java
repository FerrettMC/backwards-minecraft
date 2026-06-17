package com.ferrett.backwardsmc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class BackwardsWorldData extends SavedData {

    public boolean hasEndSpawn = false;
    public BlockPos endSpawn = BlockPos.ZERO;

    public BackwardsWorldData() {}

    // -----------------------------
    // LOAD FROM NBT
    // -----------------------------
    public static BackwardsWorldData load(CompoundTag tag) {
        BackwardsWorldData data = new BackwardsWorldData();

        data.hasEndSpawn = tag.getBoolean("hasEndSpawn").orElse(false);

        CompoundTag posTag = tag.getCompound("endSpawn").orElseGet(CompoundTag::new);

        int x = posTag.getInt("x").orElse(0);
        int y = posTag.getInt("y").orElse(0);
        int z = posTag.getInt("z").orElse(0);

        data.endSpawn = new BlockPos(x, y, z);

        return data;
    }

    // -----------------------------
    // SAVE TO NBT
    // -----------------------------
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("hasEndSpawn", hasEndSpawn);

        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", endSpawn.getX());
        posTag.putInt("y", endSpawn.getY());
        posTag.putInt("z", endSpawn.getZ());

        tag.put("endSpawn", posTag);

        return tag;
    }

    // -----------------------------
    // REQUIRED: A CODEC
    // (We don't use it, but SavedDataType requires it)
    // -----------------------------
    public static final Codec<BackwardsWorldData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.BOOL.fieldOf("hasEndSpawn").orElse(false).forGetter(d -> d.hasEndSpawn),
                            BlockPos.CODEC.fieldOf("endSpawn").orElse(BlockPos.ZERO).forGetter(d -> d.endSpawn)
                    ).apply(instance, (has, pos) -> {
                        BackwardsWorldData d = new BackwardsWorldData();
                        d.hasEndSpawn = has;
                        d.endSpawn = pos;
                        return d;
                    })
            );

    // -----------------------------
    // THE SavedDataType YOUR API EXPECTS
    // -----------------------------
    public static final SavedDataType<BackwardsWorldData> TYPE =
            new SavedDataType<>(
                    "backwardsminecraft_worlddata",
                    BackwardsWorldData::new,   // constructor
                    CODEC                      // codec
            );

    // -----------------------------
    // GET INSTANCE
    // -----------------------------
    public static BackwardsWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }


}
