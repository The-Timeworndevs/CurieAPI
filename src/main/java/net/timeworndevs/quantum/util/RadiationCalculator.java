package net.timeworndevs.quantum.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.timeworndevs.quantum.radiation.RadiationType;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.timeworndevs.quantum.util.QuantumConfig.*;
import static net.timeworndevs.quantum.util.QuantumConfig.BIOME_RADIATION_VALUES;

public class RadiationCalculator {
    public static int calculateInventoryRadiation(ServerPlayerEntity player, RadiationType type) {
        int radiationFromItems = 0;
        // Search through inventory to find radioactive items.
        for (ItemStack itemStack : player.getInventory().main) {
            Item item = itemStack.getItem();
            if (ITEM_RADIATION_VALUES.containsKey(item) && ITEM_RADIATION_VALUES.get(item).containsKey(type)) {

                radiationFromItems += ITEM_RADIATION_VALUES.get(item).get(type) * itemStack.getCount();
            }
        }
        return radiationFromItems;
    }
    public static float calculateBiomeRadiation(ServerWorld world, ServerPlayerEntity player) {
        float biomeMultiplier = 0;
        Optional<RegistryKey<Biome>> biome = world.getBiome(player.getBlockPos()).getKey();

        if (biome.isPresent()) {
            String biomeID = biome.get().getValue().toString();
            // Check if biome is in BIOME_RADIATION_VALUES then adds a multiplier.
            if (BIOME_RADIATION_VALUES.containsKey(biomeID)) {
                for (int value : BIOME_RADIATION_VALUES.get(biomeID).values()) {
                    biomeMultiplier += value;
                }
                // If the player is in the overworld, also apply more radiation if exposed to light.
                if (player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
                    biomeMultiplier = biomeMultiplier * (world.getLightLevel(LightType.SKY, player.getBlockPos()) / 15.0f);
                }
            }
        }
        return biomeMultiplier;
    }
    // Raycasts from the player to the radioactive block
    private static BlockHitResult raycastInsulator(RaycastContext context, Predicate<BlockState> statePredicate, BlockPos ignored, ServerPlayerEntity player) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            World world = player.getWorld();
            if (pos.equals(ignored)) {
                return null;
            }
            Vec3d start = innerContext.getStart();
            Vec3d end = innerContext.getEnd();
            BlockState blockState = world.getBlockState(pos);
            if (!statePredicate.test(blockState)) {
                return null;
            }
            VoxelShape voxelShape = innerContext.getBlockShape(blockState, world, pos);
            return world.raycastBlock(start, end, pos, voxelShape, blockState);

        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    // Checks if there are insulator blocks present between the player and the radioactive block.
    public static int calculateInsulators(ServerPlayerEntity player, RadiationType type, BlockPos blockPos) {
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

    // Calculates how many radioactive blocks are within a 11x11 box from the player.
    public static int calculateBlockRadiation(ServerPlayerEntity player, RadiationType type) {
        int radiation = 0;

        Stream<BlockPos> blockPosStream = BlockPos.stream(player.getBoundingBox().expand(5))
                .filter(blockPos -> player.getWorld().getBlockState(blockPos).getBlock().equals(Blocks.AIR))
                .filter(blockPos -> BLOCK_RADIATION_VALUES.containsKey(player.getWorld().getBlockState(blockPos).getBlock()));
        radiation += blockPosStream.mapToInt((blockPos) -> {
                    int insulatorValue = calculateInsulators(player, type, blockPos);
                    return Math.max(0, BLOCK_RADIATION_VALUES.get(player.getWorld().getBlockState(blockPos).getBlock()).get(type) - insulatorValue);
                })
                .reduce(0, Integer::sum);
        return radiation;
    }

    // Calculates the amount of radiation to add to the player (for a period of time) for a type of radiation
    public static int calculateRadiationForType(ServerWorld world, ServerPlayerEntity player, RadiationType type) {
        float biomeMultiplier = calculateBiomeRadiation(world, player);
        int radiationAround = calculateBlockRadiation(player, type);
        int radiationFromItems = calculateInventoryRadiation(player, type);
        float armorProtection = 0;


        // Search through armor to find insulators.
        for (ItemStack itemStack : player.getInventory().armor) {
            Item item = itemStack.getItem();
            ArmorInsulator insulator = findSetForItem(item);
            // Checks if there is a valid insulator and if it can negate this type of radiation.
            if (insulator != null && insulator.getRadiation(type) != 0) {
                armorProtection += insulator.getMultiplier(item) * insulator.getRadiation(type) * 100;
            }
        }
        return Math.round((radiationAround + radiationFromItems + biomeMultiplier) * (100 - Math.min(armorProtection, 100))) / divConstant;
    }
}
