package dev.saperate.easypronouns.mixin;

import dev.saperate.easypronouns.data.Pronouns;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
    private void init(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if(player instanceof FakePlayer || player.getEntityWorld().isClient()){
            return;
        }
        Text originalName = cir.getReturnValue();
        MutableText displayName = MutableText.of(originalName.getContent());
        Pronouns.PronounsData pronounsData = Pronouns.getPlayerData(player);
        String pronounString = pronounsData.getPronounsAsString();

        displayName.setStyle(originalName.getStyle().withHoverEvent(
                new HoverEvent.ShowText(Text.of(pronounString))
        ));
        cir.setReturnValue(displayName);
    }
    
    
    

}
