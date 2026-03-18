package dev.saperate.easypronouns.mixin;

import dev.saperate.easypronouns.EasyPronouns;
import dev.saperate.easypronouns.data.Pronouns;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.mixin.event.lifecycle.PlayerListMixin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerListEntryMixin {
    

    @Inject(at = @At("RETURN"), method = "getTabListDisplayName", cancellable = true)
    private void init(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if(player instanceof FakePlayer || !EasyPronouns.getConfig().displaysOnTabList()){
            return;
        }
        Component originalName = cir.getReturnValue();
        if(originalName == null){
            originalName = Component.translationArg(player.getName());
        }
        Pronouns.PronounsData pronounsData = Pronouns.getPlayerData(player);
        if(pronounsData.isEmpty(player)){
            cir.setReturnValue(originalName);
            return;
        }
        MutableComponent displayName = MutableComponent.create(originalName.getContents());
        String pronounString = pronounsData.getPronounsAsString();

        displayName.append(" (").append(pronounString).append(")");
        displayName.setStyle(originalName.getStyle());
        cir.setReturnValue(displayName);
    }

}
