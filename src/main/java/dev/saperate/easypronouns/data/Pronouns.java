package dev.saperate.easypronouns.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.saperate.easypronouns.EasyPronouns;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import java.util.*;

public class Pronouns {
    public static final AttachmentType<PronounsData> PRONOUNS_ATTACHMENT_TYPE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(EasyPronouns.MODID, "pronouns"),
            builder->builder
                    .initializer(()->PronounsData.DEFAULT)
                    .persistent(PronounsData.CODEC)
                    .syncWith(
                            PronounsData.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );
    
    public static PronounsData getPlayerData(AttachmentTarget target) {
        return target.getAttachedOrCreate(PRONOUNS_ATTACHMENT_TYPE);
    }

    public static void InitialiseDataTypes(){
        
    }
    
    public record PronounsData(List<String> pronounsList) {
        public static Codec<PronounsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_EMPTY_STRING.listOf().fieldOf("validRails").forGetter(PronounsData::pronounsList)
        ).apply(instance, PronounsData::new));

        public static StreamCodec<ByteBuf, PronounsData> PACKET_CODEC = ByteBufCodecs.fromCodec(CODEC);

        public static PronounsData DEFAULT = new PronounsData(new ArrayList<>());


        public List<String> getPronounsAsList(AttachmentTarget target) {
            return new ArrayList<>(pronounsList);
        }
        
        public String getPronounsAsString(){
            return getPronounsAsString("/");
        }

        public String getPronounsAsString(String delimiter){
            List<String> pronouns = pronounsList;
            if(pronouns.isEmpty())
                return "";
            StringBuilder builder = new StringBuilder(pronouns.size());
            for (String pronoun : pronouns){
                builder.append(pronoun).append(delimiter);
            }
            return builder.substring(0,builder.length() - delimiter.length());//Remove the last delimiter
        }

        public void addPronoun(AttachmentTarget target, String pronoun) {
            target.modifyAttached(PRONOUNS_ATTACHMENT_TYPE, pronouns -> {
                ArrayList<String> pronounsClone = new ArrayList<>(pronouns.pronounsList);
                pronounsClone.add(pronoun);
                pronouns = new PronounsData(pronounsClone);
                return pronouns;
            });
        }

        public void removePronoun(AttachmentTarget target, String pronoun) {
            target.modifyAttached(PRONOUNS_ATTACHMENT_TYPE, pronouns -> {
                ArrayList<String> pronounsClone = new ArrayList<>(pronouns.pronounsList);
                pronounsClone.remove(pronoun);
                pronouns = new PronounsData(pronounsClone);
                return pronouns;
            });
        }
        
        public boolean hasPronoun(String pronoun){
            return pronounsList.contains(pronoun);
        }
        
        public int pronounCount(){
            return pronounsList.size();
        }

        public boolean isEmpty(AttachmentTarget target){
            return pronounsList.isEmpty();
        }
        
    }
}
