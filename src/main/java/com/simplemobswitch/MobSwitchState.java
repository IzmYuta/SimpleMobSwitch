package com.simplemobswitch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import net.minecraft.world.PersistentStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobSwitchState extends PersistentState {
    private static final String IDENTIFIER = "mob_switch_state";
    private boolean isActive = false;
    private final List<UUID> dummyMobUUIDs = new ArrayList<>();

    // コーデックの定義
    public static final Codec<MobSwitchState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("isActive").forGetter(state -> state.isActive),
            Codec.STRING.listOf().xmap(
                    strings -> strings.stream().map(UUID::fromString).collect(java.util.stream.Collectors.toList()),
                    uuids -> uuids.stream().map(UUID::toString).collect(java.util.stream.Collectors.toList()))
                    .fieldOf("dummyMobs").forGetter(state -> state.dummyMobUUIDs))
            .apply(instance, (isActive, dummyMobs) -> {
                MobSwitchState state = new MobSwitchState();
                state.isActive = isActive;
                state.dummyMobUUIDs.addAll(dummyMobs);
                return state;
            }));

    public MobSwitchState() {
        super();
    }

    public static MobSwitchState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(
                        new PersistentStateType<>(
                                IDENTIFIER,
                                () -> new MobSwitchState(),
                                CODEC,
                                null // DataFixTypes - 現在の実装では不要
                        ));
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        this.markDirty();
    }

    public List<UUID> getDummyMobUUIDs() {
        return dummyMobUUIDs;
    }

    public void clearDummyMobs() {
        dummyMobUUIDs.clear();
        this.markDirty();
    }

    public void addDummyMob(UUID uuid) {
        dummyMobUUIDs.add(uuid);
        this.markDirty();
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("isActive", isActive);

        NbtList uuidList = new NbtList();
        for (UUID uuid : dummyMobUUIDs) {
            uuidList.add(NbtString.of(uuid.toString()));
        }
        nbt.put("dummyMobs", uuidList);

        return nbt;
    }

    public static MobSwitchState createFromNbt(NbtCompound nbt) {
        MobSwitchState state = new MobSwitchState();
        state.isActive = nbt.contains("isActive") && nbt.getBoolean("isActive").orElse(false);

        if (nbt.contains("dummyMobs")) {
            NbtList uuidList = (NbtList) nbt.get("dummyMobs");
            for (int i = 0; i < uuidList.size(); i++) {
                NbtElement element = uuidList.get(i);
                if (element instanceof NbtString) {
                    String uuidStr = element.asString().orElse(null);
                    if (uuidStr != null) {
                        state.dummyMobUUIDs.add(UUID.fromString(uuidStr));
                    }
                }
            }
        }

        return state;
    }
}