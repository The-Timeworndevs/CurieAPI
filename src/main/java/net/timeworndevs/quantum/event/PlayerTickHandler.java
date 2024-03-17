package net.timeworndevs.quantum.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.networking.ModMessages;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;
import org.apache.logging.log4j.Marker;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

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
                    for (String i: Quantum.new_radiation_types.keySet()) {
                        Quantum.LOGGER.info(String.valueOf(((IEntityDataSaver) player).getPersistentData().getInt("radiation."+i)));
                        if ((calculateRadiation(world, player, i))>0) {
                            ClientPlayNetworking.send(new Identifier(Quantum.MOD_ID,"radiation_"+i), PacketByteBufs.create());
                        } else {
                            if (((IEntityDataSaver) player).getPersistentData().getInt("radiation." + i)>0) {
                                ClientPlayNetworking.send(new Identifier(Quantum.MOD_ID,"radiation_"+i+"_del"), PacketByteBufs.create());
                            }
                        }
                        Quantum.LOGGER.info(i + ":" + String.valueOf(calculateRadiation(world, player, i)));
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
        if (Quantum.radiation_data!=null) {
            for (String key : Quantum.radiation_data.keySet()) {
                JsonObject curr = Quantum.radiation_data.get(key);

                if (curr.has("biomes")) {
                    for (JsonElement element : curr.get("biomes").getAsJsonArray()) {
                        if (Objects.equals(biome, element.getAsJsonObject().get("object").getAsString())) {
                            if (element.getAsJsonObject().has(kind)) {
                                biomeMultiplier += element.getAsJsonObject().get(kind).getAsInt();
                            }
                        }
                        //loop trough jsons and check block, correct radiation level and radiation type... instead of blindly hard coding that

                    }
                }

                if (curr.has("blocks")) {
                    for (JsonElement element : curr.get("blocks").getAsJsonArray()) {
                        if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                            if (element.getAsJsonObject().has(kind)) {
                                radiationAround += element.getAsJsonObject().get(kind).getAsInt() * BlockPos.stream(player.getBoundingBox().expand(10))
                                        .map(world::getBlockState).filter(state -> state.isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())))).toArray().length;
                            }
                        }
                    }
                }


                if (curr.has("items")) {
                    for (JsonElement element : curr.get("items").getAsJsonArray()) {
                        if (!Objects.equals(Registries.ITEM.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                            for (int i = 0; i < player.getInventory().size(); i++) {

                                if (Registries.ITEM.get(new Identifier(element.getAsJsonObject().get("object").getAsString())) == player.getInventory().getStack(i).getItem()) {
                                    if (element.getAsJsonObject().has(kind)) {
                                        radiationFromItems += element.getAsJsonObject().get(kind).getAsInt() * player.getInventory().getStack(i).getCount();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return radiationAround+radiationFromItems+biomeMultiplier;
    }
    public static double calculateDivision(ServerPlayerEntity player, String kind) {
        double radiationDivision = 1; //blocked %. RADIATION/THIS INT

        if (Quantum.radiation_data!=null) {
            for (String key : Quantum.radiation_data.keySet()) {
                JsonObject curr = Quantum.radiation_data.get(key);

                if (curr.has("blocks")) {
                    for (JsonElement element : curr.get("blocks").getAsJsonArray()) {
                        if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                            if (element.getAsJsonObject().has(kind)) {

                                Quantum.LOGGER.info(String.valueOf(BlockPos.stream(player.getBoundingBox().expand(10)).toArray().length));
                                for (Object i: BlockPos.stream(player.getBoundingBox().expand(10)).toArray()) {
                                    if (!player.getWorld().getBlockState((BlockPos) i).isAir()) {
                                        Quantum.LOGGER.info("WOA");
                                    }
                                    if (player.getWorld().getBlockState((BlockPos) i).isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())))) {

                                        Vec3d start, end;

                                        start = player.getPos();
                                        end = ((BlockPos) i).toCenterPos();

                                        Quantum.LOGGER.info(String.valueOf(start));
                                        Quantum.LOGGER.info(String.valueOf(end));


                                        RaycastContext.ShapeType blockContext;

                                        blockContext = RaycastContext.ShapeType.COLLIDER;


                                        RaycastContext.FluidHandling fluidContext;

                                        fluidContext = RaycastContext.FluidHandling.NONE;


                                        BlockHitResult result = player.getWorld().raycast(new RaycastContext(start, end, blockContext, fluidContext, new MarkerEntity(EntityType.MARKER, player.getWorld())));//new RaycastContext(start, end, blockContext, fluidContext, new Marker(EntityType.MARKER, player.getWorld())));

                                    Quantum.LOGGER.info(String.valueOf(result.getBlockPos()));
                                    }
                                }
                            }
                        }
                    }
                }
                    /*for (JsonElement element : curr.get("insulators").getAsJsonArray()) {
                        if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                            if (element.getAsJsonObject().has(kind)) {

                                radiationDivision += element.getAsJsonObject().get(kind).getAsDouble() * BlockPos.stream(player.getBoundingBox().expand(10))
                                        .map(((ServerWorld) player.getWorld())::getBlockState).filter(state -> state.isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())))).toArray().length;
                            }
                        }
                    }*/


                if (curr.has("armor")) {
                    for (JsonElement element : curr.get("armor").getAsJsonArray()) {
                        for (String part : new String[]{"boots", "leggings", "chestplate", "helmet"}) {
                            if (!Objects.equals(Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString())).toString(), "minecraft:air")) {
                                for (int i = 0; i < 4; i++) {
                                    if (player.getInventory().armor.get(i).getItem() == Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString()))) {
                                        if (element.getAsJsonObject().has(kind)) {
                                            radiationDivision += element.getAsJsonObject().get(kind).getAsDouble();
                                        }
                                    }
                                }


                            }
                        }
                    }
                }
            }
        }

        return radiationDivision;
    }

}