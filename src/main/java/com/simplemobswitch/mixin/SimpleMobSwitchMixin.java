package com.simplemobswitch.mixin;

import com.simplemobswitch.SimpleMobSwitch;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class SimpleMobSwitchMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		// サーバーワールドロード時の処理
		SimpleMobSwitch.LOGGER.info("SimpleMobSwitch Mixin initialized");
	}
}