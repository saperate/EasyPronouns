package dev.saperate.easypronouns.mixin;

import dev.saperate.easypronouns.EasyPronouns;
import dev.saperate.easypronouns.data.Pronouns;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.mixin.event.lifecycle.PlayerListMixin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.dedicated.gui.PlayerListGui;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerListEntryMixin {
    

    @Inject(at = @At("RETURN"), method = "getPlayerListName", cancellable = true)
    private void init(CallbackInfoReturnable<Text> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if(player instanceof FakePlayer || !EasyPronouns.getConfig().displaysOnTabList()){
            return;
        }
        Text originalName = cir.getReturnValue();
        if(originalName == null){
            originalName = Text.of(player.getName());
        }
        Pronouns.PronounsData pronounsData = Pronouns.getPlayerData(player);
        if(pronounsData.isEmpty(player)){
            cir.setReturnValue(originalName);
            return;
        }
        MutableText displayName = MutableText.of(originalName.getContent());
        String pronounString = pronounsData.getPronounsAsString();

        displayName.append(" (").append(pronounString).append(")");
        displayName.setStyle(originalName.getStyle());
        cir.setReturnValue(displayName);
    }

}
