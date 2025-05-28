package com.simplemobswitch;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MobSwitchState extends PersistentState {
    private static final String IDENTIFIER = "mob_switch_state";
    private boolean isActive = false;
    private final List<UUID> dummyMobUUIDs = new ArrayList<>();

    public MobSwitchState() {
        super();
    }

    public static MobSwitchState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(
                        new PersistentStateType<>(
                                IDENTIFIER,
                                () -> new MobSwitchState(),
                                null, // codec - 現在の実装では不要
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