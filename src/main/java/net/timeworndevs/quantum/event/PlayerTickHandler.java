package net.timeworndevs.quantum.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.timeworndevs.quantum.Quantum;
import net.minecraft.world.RaycastContext;
import net.timeworndevs.quantum.radiation.RadiationData;
import net.timeworndevs.quantum.radiation.RadiationType;
import net.timeworndevs.quantum.util.ArmorInsulator;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.QuantumConfig;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.timeworndevs.quantum.util.QuantumConfig.*;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private int tick = 0;
    public static HashMap<UUID, Boolean> playersConnected = new HashMap<>();
    @Override
    public void onStartTick(MinecraftServer server) {
        if (tick >= 20) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = (ServerWorld) player.getWorld();
                Quantum.LOGGER.info(String.valueOf(world.getBiome(player.getBlockPos()).matchesId(new Identifier("plains"))));
                if (playersConnected.getOrDefault(player.getUuid(), false)) {
                    for (RadiationType type : RadiationType.RADIATION_TYPES.values()) {
                        int radiation = calculateRadiation(world, player, type);
                        if (radiation > 0) {
                            RadiationData.addRad((IEntityDataSaver) player, type, radiation);
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
    public static int calculateRadiation(ServerWorld world, ServerPlayerEntity player, RadiationType type) {
        float biomeMultiplier = 0.0f;
        int radiationAround = calculateBlockRadiation(player, type);;
        int radiationFromItems = 0;
        float armorProtection = 0;
        Optional<RegistryKey<Biome>> biome = world.getBiome(player.getBlockPos()).getKey();

        if (biome.isPresent()) {
            String biomeID = biome.get().getValue().toString();
            if (QuantumConfig.BIOME_RADIATION_VALUES.containsKey(biomeID)) {
                for (int value : QuantumConfig.BIOME_RADIATION_VALUES.get(biomeID).values()) {
                    biomeMultiplier += value;
                }

                if (player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
                    biomeMultiplier = biomeMultiplier * (world.getLightLevel(LightType.SKY, player.getBlockPos()) / (float) 15);
                }
            }
        }
        for (ItemStack itemStack : player.getInventory().main) {
            Item item = itemStack.getItem();
            if (ITEM_RADIATION_VALUES.containsKey(item)) {
                radiationFromItems += ITEM_RADIATION_VALUES.get(item).get(type);
            }
        }
        for (ItemStack itemStack : player.getInventory().armor) {
            Item item = itemStack.getItem();
            ArmorInsulator insulator = QuantumConfig.findSetForItem(item);
            if (insulator != null) {
                armorProtection += insulator.getMultiplier(item) * 100;
            }

        }
        return Math.round((radiationAround + radiationFromItems + biomeMultiplier) * (100 - Math.min(armorProtection, 100))) / divConstant;
    }

    private static BlockHitResult raycastInsulator(RaycastContext context, Predicate<BlockState> statePredicate, BlockPos ignored, ServerPlayerEntity player) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            if (pos.equals(ignored)) {
                return null;
            }
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();
            BlockState blockState = player.getWorld().getBlockState(pos);
            if (!statePredicate.test(blockState)) {
                return null;
            }
            VoxelShape voxelShape = innerContext.getBlockShape(blockState, player.getWorld(), pos);
            return player.getWorld().raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);

        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    private static int calculateInsulators(ServerPlayerEntity player, RadiationType type, BlockPos blockPos) {
        int totalInsulation = 0;
        Vec3d start, end;

        start = player.getPos().add(0.0, 0.1, 0.0);
        end = (blockPos).toCenterPos();

        boolean reachedEnd = false;
        BlockPos lastBlockPos = null;
        while (!reachedEnd) {
            BlockHitResult result = raycastInsulator(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player), (blockState) -> {
                    Integer insulatorValue = INSULATORS.get(blockState.getBlock()).get(type);
                    return insulatorValue != null;
                }, lastBlockPos, player);

            lastBlockPos = result.getBlockPos();
            if (lastBlockPos.equals(blockPos)) {
                reachedEnd = true;
            } else {

                Integer insulatorValue = INSULATORS.get(player.getWorld().getBlockState(lastBlockPos).getBlock()).get(type);
                totalInsulation += insulatorValue != null ? insulatorValue : 0;
                start = result.getPos();
            }
        }

        return totalInsulation;
    }

    public static int calculateBlockRadiation(ServerPlayerEntity player, RadiationType type) {
        int radiation = 0;

        Stream<BlockPos> blockPosStream = BlockPos.stream(player.getBoundingBox().expand(5))
                .filter(blockPos -> BLOCK_RADIATION_VALUES.containsKey(player.getWorld().getBlockState(blockPos).getBlock()));
        radiation += blockPosStream.mapToInt((blockPos) -> {
                    int insulatorValue = calculateInsulators(player, type, blockPos);
                    return Math.max(0, BLOCK_RADIATION_VALUES.get(player.getWorld().getBlockState(blockPos).getBlock()).get(type) - insulatorValue);
                })
                .reduce(0, Integer::sum);
        return radiation;
    }
}