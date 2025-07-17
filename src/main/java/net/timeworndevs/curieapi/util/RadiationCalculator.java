package net.timeworndevs.curieapi.util;

import net.minecraft.block.Block;
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
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

import static net.timeworndevs.curieapi.util.CurieAPIConfig.*;

public class RadiationCalculator {
    public static int calculateInventoryRadiation(ServerPlayerEntity player, RadiationType type) {
        int radiationFromItems = 0;

        // Search through inventory to find radioactive items.
        for (ItemStack itemStack : player.getInventory().main) {
            if (!itemStack.isEmpty()) {
                Item item = itemStack.getItem();
                if (ITEM_RADIATION_VALUES.containsKey(item)) {
                    radiationFromItems += ITEM_RADIATION_VALUES.get(item).getOrDefault(type, 0) * itemStack.getCount();
                }
            }
        }
        return radiationFromItems;
    }
    public static float calculateBiomeRadiation(ServerWorld world, ServerPlayerEntity player, RadiationType type) {
        float biomeMultiplier = 0;
        Optional<RegistryKey<Biome>> biome = world.getBiome(player.getBlockPos()).getKey();

        if (biome.isPresent()) {
            String biomeID = biome.get().getValue().toString();
            // Check if biome is in BIOME_RADIATION_VALUES then adds a multiplier.
            if (BIOME_RADIATION_VALUES.containsKey(biomeID)) {
                biomeMultiplier = BIOME_RADIATION_VALUES.get(biomeID).getOrDefault(type, 0);

                // If the player is in the overworld, also apply more radiation if exposed to light.
                if (world.getRegistryKey().equals(World.OVERWORLD)) {
                    biomeMultiplier = biomeMultiplier * (world.getLightLevel(LightType.SKY, player.getBlockPos()) / 15.0f);
                }
            }
        }
        return biomeMultiplier;
    }
    // Raycasts from the player to the radioactive block
    private static BlockHitResult raycastInsulator(RaycastContext context, Predicate<BlockState> statePredicate, BlockPos ignored, ServerWorld world) {

        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
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
    public static int calculateInsulators(ServerWorld world, ServerPlayerEntity player, RadiationType type, BlockPos blockPos) {
        int totalInsulation = 0;
        Vec3d start, end;

        start = player.getPos().add(0.0, 0.1, 0.0);
        end = blockPos.toCenterPos();

        boolean reachedEnd = false;
        BlockPos lastBlockPos = null;
        while (!reachedEnd) {
            BlockHitResult result = raycastInsulator(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player), (blockState) -> INSULATORS.containsKey(blockState.getBlock()) && INSULATORS.get(blockState.getBlock()).containsKey(type), lastBlockPos, world);
            lastBlockPos = result.getBlockPos();
            if (lastBlockPos.equals(blockPos)) {
                reachedEnd = true;
            } else {
                HashMap<RadiationType, Integer> map = INSULATORS.get(world.getBlockState(lastBlockPos).getBlock());
                if (map != null) {
                    totalInsulation += map.getOrDefault(type, 0);
                }
                start = result.getPos();
            }
        }
        return totalInsulation;
    }

    // Calculates how many radioactive blocks are within a 11x11 box from the player.
    public static int calculateBlockRadiation(ServerWorld world, ServerPlayerEntity player, RadiationType type) {
        int radiation = 0;
        BlockPos corner1 = player.getBlockPos().add(-5, -5, -5);
        BlockPos corner2 = player.getBlockPos().add(5, 5, 5);

        for (BlockPos pos : BlockPos.iterate(corner1, corner2)) {
            Block block = world.getBlockState(pos).getBlock();
            if (!block.equals(Blocks.AIR) && BLOCK_RADIATION_VALUES.containsKey(block)) {
                int insulatorValue = calculateInsulators(world, player, type, pos);
                radiation += Math.max(0, BLOCK_RADIATION_VALUES.get(block).getOrDefault(type, 0) - insulatorValue);
            }
        }
        return radiation;
    }

    // Calculates the amount of radiation to add to the player (for a period of time) for a type of radiation
    public static int calculateRadiationForType(ServerWorld world, ServerPlayerEntity player, RadiationType type) {
        float biomeMultiplier = calculateBiomeRadiation(world, player, type);
        int radiationAround = calculateBlockRadiation(world, player, type);
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
