package net.timeworndevs.quantum.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.BlockView;
import net.timeworndevs.quantum.Quantum;
import net.minecraft.world.RaycastContext;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private int tick = 0;
    public static HashMap<UUID, Boolean> playersConnected = new HashMap<UUID, Boolean>();
    @Override
    public void onStartTick(MinecraftServer server) {
        if (tick >= 20) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (playersConnected.getOrDefault(player.getUuid(), false)) {
                    ServerWorld world = (ServerWorld) player.getWorld();

                    int alpha = calculateRadiation(world, player, "alpha");
                    int beta = calculateRadiation(world, player, "beta");
                    int gamma = calculateRadiation(world, player, "gamma");
                    if (alpha > 0) {
                        Quantum.LOGGER.info("Alpha: " + alpha);
                        RadiationData.addRad((IEntityDataSaver) player, "alpha", alpha);
                    } else {
                        RadiationData.delRad((IEntityDataSaver) player, "alpha", 1);
                    }

                    if (beta > 0) {
                        Quantum.LOGGER.info("Beta: " + beta);
                        RadiationData.addRad((IEntityDataSaver) player, "beta", beta);
                    } else {
                        RadiationData.delRad((IEntityDataSaver) player, "beta", 1);
                    }

                    if (gamma > 0) {
                        Quantum.LOGGER.info("Gamma: " + gamma);
                        RadiationData.addRad((IEntityDataSaver) player, "gamma", gamma);
                    } else {
                        RadiationData.delRad((IEntityDataSaver) player, "gamma", 1);
                    }

                    for (String type : Quantum.new_radiation_types.keySet()) {
                        int radData = calculateRadiation(world, player, type);
                        if (radData > 0) {
                            Quantum.LOGGER.info(type + ": " + radData);
                            RadiationData.addRad((IEntityDataSaver) player, type, radData);
                        } else {
                            RadiationData.delRad((IEntityDataSaver) player, type, 1);
                        }
                    }
                }
            }
            tick = 0;
        } else {
            tick++;
        }
    }

    public static void addConnectedPlayer(UUID playerUUID) {
        playersConnected.put(playerUUID, true);
    }
    public static void removeConnectedPlayer(UUID playerUUID) {
        playersConnected.remove(playerUUID);
    }

    // Calculates the amount of radiation to add to the player (for a period of time) for a type of radiation
    public static int calculateRadiation(ServerWorld world, ServerPlayerEntity player, String kind) {
        int biomeMultiplier = 0;
        String biome = world.getBiome(player.getBlockPos()).getKey().toString().replace("Optional[ResourceKey[minecraft:worldgen/biome / ", "").replace("]]", ""); // I'm the worst dev hello there for doing that

        //loop trough jsons and check biome, correct radiation level and radiation type... instead of blindly hard coding that
        int radiationFromItems = 0;
        int radiationAround = 0;
        double armorProtection = 0;
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

                radiationAround += calculateBlockRadiation(player, kind);
                //Quantum.LOGGER.info(kind + radiationAround);


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
                if (curr.has("armor")) {
                    for (JsonElement element : curr.get("armor").getAsJsonArray()) {
                        for (String part : new String[]{"boots", "leggings", "chestplate", "helmet"}) {
                            if (!Objects.equals(Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString())).toString(), "minecraft:air")) {
                                for (int i = 0; i < 4; i++) {
                                    if (player.getInventory().armor.get(i).getItem() == Registries.ITEM.get(new Identifier(element.getAsJsonObject().get(part).getAsString()))) {
                                        if (element.getAsJsonObject().has(kind)) {
                                            armorProtection += element.getAsJsonObject().get(kind).getAsDouble();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return (int) (radiationAround+radiationFromItems+biomeMultiplier * ((100 - Math.min(armorProtection, 100))/100) );
    }

    private static BlockHitResult raycastInsulator(RaycastContext context, Predicate<BlockState> statePredicate, BlockPos ignored, ServerPlayerEntity player) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            if (pos.equals(ignored)) {
                return null;
            }
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();
            BlockState blockState = player.getWorld().getBlockState((BlockPos)pos);
            if (! statePredicate.test(blockState)) {
                return null;
            }
            VoxelShape voxelShape = innerContext.getBlockShape(blockState, player.getWorld(), (BlockPos)pos);
            return player.getWorld().raycastBlock(vec3d, vec3d2, (BlockPos)pos, voxelShape, blockState);

        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    private static int calculateInsulators(ServerPlayerEntity player, String kind, BlockPos blockPos) {
//        Quantum.LOGGER.info(String.valueOf(player.getWorld().getBlockState(blockPos).getBlock().getName()));
        int total_insulation = 0;
        Vec3d start, end;

        start = player.getPos();
        end = (blockPos).toCenterPos();

        //Find list of insulators from configs and put into hashmap for easier use
        HashMap<String, Integer> insulators = new HashMap<String, Integer>();
        if (Quantum.radiation_data!=null) {
            for (String key: Quantum.radiation_data.keySet()) {
                JsonObject curr = Quantum.radiation_data.get(key);
                if (curr.has("insulators")) {
                    for (JsonElement element : curr.get("insulators").getAsJsonArray()) {
                        if (element.getAsJsonObject().has(kind)) {
                            insulators.put(element.getAsJsonObject().get("object").getAsString(), element.getAsJsonObject().get(kind).getAsInt());
                        }
                    }
                }
            }
        }
//        Quantum.LOGGER.info(insulators.toString());


        boolean reachedEnd = false;
        BlockPos lastBlockPos = null;
        while (!reachedEnd) {
            BlockHitResult result = raycastInsulator(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player), (blockState) -> {
                    Integer insulatorValue = insulators.get(Registries.BLOCK.getId(blockState.getBlock()).toString());
                    return insulatorValue != null;
                }, lastBlockPos, player);

            lastBlockPos = result.getBlockPos();
            if (lastBlockPos.equals(blockPos)) {
                reachedEnd = true;
            } else {
                Integer insulatorValue = insulators.get(Registries.BLOCK.getId(player.getWorld().getBlockState(lastBlockPos).getBlock()).toString());
                total_insulation += insulatorValue != null ? insulatorValue : 0;
                start = result.getPos();
            }
        }

        return total_insulation;
    }

    public static int calculateBlockRadiation(ServerPlayerEntity player, String kind) {
        int radiation = 0;
        if (Quantum.radiation_data!=null) {
            for (String key : Quantum.radiation_data.keySet()) {
                JsonObject curr = Quantum.radiation_data.get(key);

                if (curr.has("blocks")) {
                    for (JsonElement element : curr.get("blocks").getAsJsonArray()) {
                        if (!Objects.equals(Registries.BLOCK.get(new Identifier ( element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
                            if (element.getAsJsonObject().has(kind)) {
                                Stream<BlockPos> blockPosStream = BlockPos.stream(player.getBoundingBox().expand(5))
                                        .filter(blockPos -> player.getWorld().getBlockState(blockPos).isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString()))));
                                radiation += blockPosStream.mapToInt((blockPos) -> {
                                            int insulatorValue = calculateInsulators(player, kind, blockPos);
                                            return Math.max(0, element.getAsJsonObject().get(kind).getAsInt()-insulatorValue);
                                        })
                                        .reduce(0, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        return radiation;
    }

    // Calculates the amount of insulation provided for a type of radiation
    public static double calculateDivision(ServerPlayerEntity player, String kind) {
        double radiationDivision = 1; //blocked %. RADIATION/THIS INT

        if (Quantum.radiation_data!=null) {
            for (String key : Quantum.radiation_data.keySet()) {
                JsonObject curr = Quantum.radiation_data.get(key);

//                if (curr.has("blocks")) {
//                    for (JsonElement element : curr.get("blocks").getAsJsonArray()) {
//                        if (!Objects.equals(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString())).toString(), "minecraft:air")) {
//                            if (element.getAsJsonObject().has(kind)) {
//                                Stream<BlockPos> blockPosStream = BlockPos.stream(player.getBoundingBox().expand(5))
//                                        .filter(blockPos -> player.getWorld().getBlockState(blockPos).isOf(Registries.BLOCK.get(new Identifier(element.getAsJsonObject().get("object").getAsString()))));
//                                blockPosStream.forEach(blockPos -> calculateInsulators(player, kind, blockPos ));
////
//                            }
//                        }
//                    }
//                }
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