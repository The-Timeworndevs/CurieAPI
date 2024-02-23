package net.timeworndevs.quantum.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.block.ModBlocks;
import net.timeworndevs.quantum.item.ModItems;
import net.timeworndevs.quantum.networking.ModMessages;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private int tick = 0;
    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if(tick>=20) {

                Quantum.LOGGER.info(((IEntityDataSaver) player).getPersistentData().getInt("radiation.alpha") + ":" + ((IEntityDataSaver) player).getPersistentData().getInt("radiation.beta") + ":"
                            + ((IEntityDataSaver) player).getPersistentData().getInt("radiation.gamma"));
                if (player.isPartOfGame()) {
                    ServerWorld world = ((ServerWorld) player.getWorld());


                    int alpha = calculateRadiation(world, player, "alpha");
                    int beta = calculateRadiation(world, player, "beta");
                    int gamma = calculateRadiation(world, player, "gamma");

                    if ((alpha)>0) {
                        ClientPlayNetworking.send(ModMessages.ALPHA_ID, PacketByteBufs.create());
                    } else {
                        if (((IEntityDataSaver) player).getPersistentData().getInt("radiation.alpha")>0) {
                            ClientPlayNetworking.send(ModMessages.ALPHA_DEL_ID, PacketByteBufs.create());
                        }
                    }
                    if ((beta)>0) {
                        ClientPlayNetworking.send(ModMessages.BETA_ID, PacketByteBufs.create());
                    } else {
                        if (((IEntityDataSaver) player).getPersistentData().getInt("radiation.beta")>0) {
                            ClientPlayNetworking.send(ModMessages.BETA_DEL_ID, PacketByteBufs.create());
                        }
                    }
                    if ((gamma)>0) {
                        ClientPlayNetworking.send(ModMessages.GAMMA_ID, PacketByteBufs.create());
                    } else {
                        if (((IEntityDataSaver) player).getPersistentData().getInt("radiation.gamma")>0) {
                            ClientPlayNetworking.send(ModMessages.GAMMA_DEL_ID, PacketByteBufs.create());
                        }
                    }
                    Quantum.LOGGER.info("ABG: " + alpha + ":" + beta + ":" + gamma);
                    //player.sendMessage(Text.literal("Removed 1/1000 alpha radiation units"));
                }
                tick = 0;
            } else {
                tick++;
            }
        }
    }

    public static int calculateRadiation(ServerWorld world, ServerPlayerEntity player, String kind) {
        int biomeMultiplier = 0;
        String biome = world.getBiome(player.getBlockPos()).getKey().toString().replace("Optional[ResourceKey[minecraft:worldgen/biome / ", "").replace("]]", ""); // I'm the worst dev hello there for doing that

        //loop trough jsons and check biome, correct radiation level and radiation type... instead of blindly hard coding that
        int radiationFromItems = 0;
        int radiationAround = 0;
        for (JsonElement element : Quantum.radiation_data.get("biomes")) {


            if (Objects.equals(biome, element.getAsJsonObject().get("object").getAsString())) {
                biomeMultiplier += element.getAsJsonObject().get(kind).getAsInt();
            }
            //loop trough jsons and check block, correct radiation level and radiation type... instead of blindly hard coding that

        }

        for (JsonElement element: Quantum.radiation_data.get("blocks")) {
            if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                radiationAround += element.getAsJsonObject().get(kind).getAsInt() * BlockPos.stream(player.getBoundingBox().expand(10))
                        .map(world::getBlockState).filter(state -> state.isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())))).toArray().length;
            }
        }


        for (JsonElement element : Quantum.radiation_data.get("items")) {
            if (!Objects.equals(Registries.ITEM.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                for (int i = 0; i < player.getInventory().size(); i++) {

                    if ( Registries.ITEM.get(new Identifier(element.getAsJsonObject().get("object").getAsString())) == player.getInventory().getStack(i).getItem() ){
                        radiationFromItems += element.getAsJsonObject().get(kind).getAsInt() * player.getInventory().getStack(i).getCount();
                    }
                }
            }

        }

        return radiationAround+radiationFromItems+biomeMultiplier;
    }
    public static double calculateDivision(ServerPlayerEntity player, String kind) {
        double radiationDivision = 1; //blocked %. RADIATION/THIS INT

        for (JsonElement element: Quantum.radiation_data.get("inductors")) {
            if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                radiationDivision += element.getAsJsonObject().get(kind).getAsDouble() * BlockPos.stream(player.getBoundingBox().expand(10))
                        .map(((ServerWorld) player.getWorld())::getBlockState).filter(state -> state.isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())))).toArray().length;
            }
        }


        for (JsonElement element: Quantum.radiation_data.get("armor")) {
            for (String part: new String[]{"boots", "leggings", "chestplate", "helmet"}) {
                if (!Objects.equals(Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString())).toString(), "minecraft:air")) {
                    for(int i=0; i<4;i++) {
                        if( player.getInventory().armor.get(i).getItem()==Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString()))) {
                            radiationDivision += element.getAsJsonObject().get(kind).getAsDouble();
                        }
                    }


                }
            }

        }
        /*Item[] hazmatd_armor = {ModItems.HAZMATD_BOOTS, ModItems.HAZMATD_LEGGINGS, ModItems.HAZMATD_CHESTPLATE, ModItems.HAZMATD_HELMET};
        Item[] hazmatc_armor = {ModItems.HAZMATC_BOOTS, ModItems.HAZMATC_LEGGINGS, ModItems.HAZMATC_CHESTPLATE, ModItems.HAZMATC_HELMET};
        Item[] hazmatb_armor = {ModItems.HAZMATB_BOOTS, ModItems.HAZMATB_LEGGINGS, ModItems.HAZMATB_CHESTPLATE, ModItems.HAZMATB_HELMET};
        Item[] hazmata_armor = {ModItems.HAZMATA_BOOTS, ModItems.HAZMATA_LEGGINGS, ModItems.HAZMATA_CHESTPLATE, ModItems.HAZMATA_HELMET};
        for (int i=0; i<4; i++) {
            if (player.getInventory().armor.get(i).getItem()==hazmatd_armor[i]) {
                radiationDivision+=0.33;
            }
            if (player.getInventory().armor.get(i).getItem()==hazmatc_armor[i]) {
                radiationDivision+=0.5;
            }
            if (player.getInventory().armor.get(i).getItem()==hazmatb_armor[i]) {
                radiationDivision+=0.75;
            }
            if (player.getInventory().armor.get(i).getItem()==hazmata_armor[i]) {
                radiationDivision+=1;
            }
        }*/
        return radiationDivision;
    }
}