package dev.saperate.easypronouns.mixin;

import dev.saperate.easypronouns.data.Pronouns;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
    private void init(CallbackInfoReturnable<Component> cir) {
        Player player = (Player)(Object)this;
        if(player instanceof FakePlayer || player.level().isClientSide()){
            return;
        }
        Component originalName = cir.getReturnValue();
        MutableComponent displayName = MutableComponent.create(originalName.getContents());
        Pronouns.PronounsData pronounsData = Pronouns.getPlayerData(player);
        String pronounString = pronounsData.getPronounsAsString();

        displayName.setStyle(originalName.getStyle().withHoverEvent(
                new HoverEvent.ShowText(Component.nullToEmpty(pronounString))
        ));
        cir.setReturnValue(displayName);
    }
}
