package com.simplemobswitch;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.entity.SpawnReason;
import java.util.function.Consumer;

import java.util.UUID;

public class SimpleMobSwitch implements ModInitializer {
	public static final String MOD_ID = "simplemobswitch";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final String DUMMY_MOB_NAME = "MobSwitchDummy";
	private static final int BASE_DUMMY_MOB_COUNT = 70; // 1人あたりの基本モブ制限数
	private static final int DUMMY_MOB_Y_LEVEL = 1000; // より高い位置に変更

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("SimpleMobSwitch initializing...");

		// コマンド登録（非推奨APIを新しいものに置き換え）
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});

		// プレイヤー参加時の処理
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MobSwitchState state = MobSwitchState.get(server);
			if (state.isActive()) {
				adjustDummyMobCount(server, state);
			}
		});

		// プレイヤー退出時の処理
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			MobSwitchState state = MobSwitchState.get(server);
			if (state.isActive()) {
				// 少し遅延させてプレイヤー数が更新されてから調整
				server.execute(() -> adjustDummyMobCount(server, state));
			}
		});

		// サーバー起動時の処理
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MobSwitchState state = MobSwitchState.get(server);
			if (state.isActive()) {
				// 前回有効だった場合は、保存されたUUIDで再スポーン
				respawnDummyMobsWithSavedUUIDs(server, state);
			}
		});

		// サーバー終了時の処理
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			MobSwitchState state = MobSwitchState.get(server);
			if (state.isActive()) {
				// ダミーモブを削除（UUIDは保持）
				removeDummyMobsOnShutdown(server, state);
			}
		});

		LOGGER.info("SimpleMobSwitch initialized successfully!");
	}

	private int calculateRequiredMobCount(MinecraftServer server) {
		int playerCount = server.getCurrentPlayerCount();
		return playerCount * BASE_DUMMY_MOB_COUNT;
	}

	private void adjustDummyMobCount(MinecraftServer server, MobSwitchState state) {
		int requiredCount = calculateRequiredMobCount(server);
		int currentCount = state.getDummyMobUUIDs().size();

		LOGGER.info("Adjusting dummy mob count: current={}, required={}, players={}",
				currentCount, requiredCount, server.getCurrentPlayerCount());

		if (requiredCount > currentCount) {
			// モブを追加
			spawnAdditionalDummyMobs(server, state, requiredCount - currentCount);
		} else if (requiredCount < currentCount) {
			// モブを削除
			removeExcessDummyMobs(server, state, currentCount - requiredCount);
		}
	}

	private void spawnAdditionalDummyMobs(MinecraftServer server, MobSwitchState state, int count) {
		ServerWorld overworld = server.getOverworld();
		BlockPos worldSpawn = overworld.getSpawnPos();

		int offsetX = 3 * 16;
		int offsetZ = 3 * 16;
		BlockPos spawnPos = new BlockPos(worldSpawn.getX() + offsetX, DUMMY_MOB_Y_LEVEL, worldSpawn.getZ() + offsetZ);

		int successCount = 0;
		for (int i = 0; i < count; i++) {
			Consumer<ShulkerEntity> callback = null;
			ShulkerEntity shulker = EntityType.SHULKER.create(
					overworld,
					callback,
					spawnPos,
					SpawnReason.COMMAND,
					false,
					false);

			if (shulker != null) {
				shulker.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
				shulker.setCustomName(Text.literal(DUMMY_MOB_NAME));
				shulker.setCustomNameVisible(false);
				shulker.setAiDisabled(true);
				shulker.setSilent(true);
				shulker.setInvulnerable(true);
				shulker.setNoGravity(true);
				shulker.setInvisible(true);

				if (overworld.spawnEntity(shulker)) {
					state.addDummyMob(shulker.getUuid());
					successCount++;
				}
			}
		}

		LOGGER.info("Spawned {} additional dummy mobs", successCount);
	}

	private void removeExcessDummyMobs(MinecraftServer server, MobSwitchState state, int count) {
		int removedCount = 0;
		UUID[] uuidsToRemove = state.getDummyMobUUIDs().toArray(new UUID[0]);

		for (int i = 0; i < Math.min(count, uuidsToRemove.length); i++) {
			UUID uuid = uuidsToRemove[i];

			for (ServerWorld world : server.getWorlds()) {
				Entity entity = world.getEntity(uuid);
				if (entity != null && entity.isAlive() &&
						entity.getType() == EntityType.SHULKER &&
						entity.getCustomName() != null &&
						DUMMY_MOB_NAME.equals(entity.getCustomName().getString())) {

					entity.remove(Entity.RemovalReason.DISCARDED);
					state.removeDummyMob(uuid);
					removedCount++;
					break;
				}
			}
		}

		LOGGER.info("Removed {} excess dummy mobs", removedCount);
	}

	private void respawnDummyMobsWithSavedUUIDs(MinecraftServer server, MobSwitchState state) {
		if (!state.isActive())
			return;

		LOGGER.info("Respawning dummy mobs with saved UUIDs...");

		ServerWorld overworld = server.getOverworld();
		BlockPos worldSpawn = overworld.getSpawnPos();

		int offsetX = 3 * 16;
		int offsetZ = 3 * 16;
		BlockPos spawnPos = new BlockPos(worldSpawn.getX() + offsetX, DUMMY_MOB_Y_LEVEL, worldSpawn.getZ() + offsetZ);

		int successCount = 0;
		for (UUID savedUUID : state.getDummyMobUUIDs()) {
			Consumer<ShulkerEntity> callback = null;
			ShulkerEntity shulker = EntityType.SHULKER.create(
					overworld,
					callback,
					spawnPos,
					SpawnReason.COMMAND,
					false,
					false);

			if (shulker != null) {
				// 保存されたUUIDを設定
				shulker.setUuid(savedUUID);
				shulker.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
				shulker.setCustomName(Text.literal(DUMMY_MOB_NAME));
				shulker.setCustomNameVisible(false);
				shulker.setAiDisabled(true);
				shulker.setSilent(true);
				shulker.setInvulnerable(true);
				shulker.setNoGravity(true);
				shulker.setInvisible(true);

				if (overworld.spawnEntity(shulker)) {
					successCount++;
				}
			}
		}

		LOGGER.info("Respawned {} dummy mobs with saved UUIDs", successCount);

		// サーバー起動後にプレイヤー数に応じて調整
		adjustDummyMobCount(server, state);
	}

	private void removeDummyMobsOnShutdown(MinecraftServer server, MobSwitchState state) {
		LOGGER.info("Removing dummy mobs on server shutdown...");

		int removedCount = 0;
		for (ServerWorld world : server.getWorlds()) {
			for (UUID uuid : state.getDummyMobUUIDs()) {
				Entity entity = world.getEntity(uuid);
				if (entity != null && entity.isAlive() &&
						entity.getType() == EntityType.SHULKER &&
						entity.getCustomName() != null &&
						DUMMY_MOB_NAME.equals(entity.getCustomName().getString())) {

					entity.remove(Entity.RemovalReason.DISCARDED);
					removedCount++;
				}
			}
		}

		LOGGER.info("Removed {} dummy mobs on shutdown (UUIDs preserved)", removedCount);
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
				CommandManager.literal("mobswitch")
						.requires(source -> source.hasPermissionLevel(2)) // OP権限必要
						.then(CommandManager.literal("on")
								.executes(this::executeMobSwitchOn))
						.then(CommandManager.literal("off")
								.executes(this::executeMobSwitchOff)));
	}

	private int executeMobSwitchOn(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		MinecraftServer server = source.getServer();
		return activateMobSwitch(source, server);
	}

	private int activateMobSwitch(ServerCommandSource source, MinecraftServer server) {
		MobSwitchState state = MobSwitchState.get(server);

		if (state.isActive()) {
			sendFeedback(source, Text.literal("モブスイッチは既に有効になっています。"), true);
			return Command.SINGLE_SUCCESS;
		}

		// プレイヤー数に応じたモブ数を計算
		int requiredMobCount = calculateRequiredMobCount(server);

		// スポーンチャンクにダミーモブをスポーン
		ServerWorld overworld = server.getOverworld();
		// ワールドのスポーンポイントを取得
		BlockPos worldSpawn = overworld.getSpawnPos();

		// スポーンチャンクの周辺（7x7の外側リング）に配置するのが最も効率的
		// 中心から3チャンク離れた位置（スポーンチャンクの外側リング）
		int offsetX = 3 * 16; // 3チャンク × 16ブロック
		int offsetZ = 3 * 16;

		// スポーンポイントから少しずらした位置にモブを配置（AI処理を減らすため）
		BlockPos spawnPos = new BlockPos(worldSpawn.getX() + offsetX, DUMMY_MOB_Y_LEVEL, worldSpawn.getZ() + offsetZ);

		int successCount = 0;
		for (int i = 0; i < requiredMobCount; i++) {
			Consumer<ShulkerEntity> callback = null;
			ShulkerEntity shulker = EntityType.SHULKER.create(
					overworld,
					callback,
					spawnPos,
					SpawnReason.COMMAND,
					false,
					false);
			if (shulker != null) {
				shulker.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
				shulker.setCustomName(Text.literal(DUMMY_MOB_NAME));
				shulker.setCustomNameVisible(false);
				shulker.setAiDisabled(true);
				shulker.setSilent(true);
				shulker.setInvulnerable(true);
				shulker.setNoGravity(true);
				shulker.setInvisible(true);

				if (overworld.spawnEntity(shulker)) {
					state.addDummyMob(shulker.getUuid());
					successCount++;
				}
			}
		}

		if (successCount > 0) {
			state.setActive(true);
			sendFeedback(source, Text.literal(String.format("モブスイッチを有効化しました。%d体のダミーモブを召喚しました。（プレイヤー数: %d人）",
					successCount, server.getCurrentPlayerCount())), true);
			LOGGER.info("Mob switch activated, spawned {} dummy mobs for {} players", successCount,
					server.getCurrentPlayerCount());
		} else {
			sendFeedback(source, Text.literal("モブスイッチの有効化に失敗しました。"), true);
			LOGGER.error("Failed to activate mob switch, no dummy mobs were spawned");
		}

		return Command.SINGLE_SUCCESS;
	}

	private int executeMobSwitchOff(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		return deactivateMobSwitch(source.getServer());
	}

	private int deactivateMobSwitch(MinecraftServer server) {
		MobSwitchState state = MobSwitchState.get(server);

		if (!state.isActive()) {
			if (server.getCommandSource() instanceof ServerCommandSource) {
				sendFeedback(server.getCommandSource(), Text.literal("モブスイッチは既に無効になっています。"), true);
			}
			return Command.SINGLE_SUCCESS;
		}

		int removedCount = 0;
		for (ServerWorld world : server.getWorlds()) {
			for (UUID uuid : state.getDummyMobUUIDs()) {
				Entity entity = world.getEntity(uuid);
				if (entity != null && entity.isAlive() &&
						entity.getType() == EntityType.SHULKER &&
						entity.getCustomName() != null &&
						DUMMY_MOB_NAME.equals(entity.getCustomName().getString())) {

					entity.remove(Entity.RemovalReason.DISCARDED);
					removedCount++;
				}
			}
		}

		// UUIDリストもクリア（完全に無効化）
		state.clearDummyMobs();
		state.setActive(false);

		if (server.getCommandSource() instanceof ServerCommandSource) {
			sendFeedback(server.getCommandSource(),
					Text.literal(String.format("モブスイッチを無効化しました。%d体のダミーモブを削除しました。", removedCount)), true);
		}

		LOGGER.info("Mob switch deactivated, removed {} dummy mobs", removedCount);
		return Command.SINGLE_SUCCESS;
	}

	// MutableTextをSupplier<Text>に変換
	private void sendFeedback(ServerCommandSource source, Text text, boolean broadcastToOps) {
		source.sendFeedback(() -> text, broadcastToOps);
	}
}