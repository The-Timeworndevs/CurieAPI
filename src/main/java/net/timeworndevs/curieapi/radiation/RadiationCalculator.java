package net.timeworndevs.curieapi.radiation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
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
import net.timeworndevs.curieapi.util.ArmorInsulator;
import net.timeworndevs.curieapi.util.PlayerCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static net.timeworndevs.curieapi.util.CurieAPIConfig.*;

public class RadiationCalculator {

    public static float calculateInventoryRadiation(RadiationType type, PlayerCache cache) {
        RadiationEntry entry = RadiationEntry.createEmpty();
        // Search through inventory to find radioactive items.
        Map<Item, Integer> items = cache.createInventoryMap();
        if (cache.inventoryEquals(items)) {
            entry = cache.getPrevItemRadiation();
        } else {
            for (Map.Entry<Item, Integer> itemEntry : items.entrySet()) {
                Item item = itemEntry.getKey();
                RadiationEntry radiationEntry = ITEM_RADIATION_VALUES.get(item);
                if (radiationEntry != null) {
                    RadiationEntry finalEntry = entry;
                    radiationEntry.entries().forEach((radiationType, value) -> finalEntry.add(radiationType, value * itemEntry.getValue()));
                }
            }
            cache.setItemRadiation(entry);
            cache.updateInventory();
        }
        return Math.min(entry.get(type), MAX_ITEM_INTAKE);
    }
    public static float calculateBiomeRadiation(ServerWorld world, ServerPlayerEntity player, RadiationType type, PlayerCache cache) {
        float biomeMultiplier = 0.0f;

        Optional<RegistryKey<Biome>> biome = world.getBiome(player.getBlockPos()).getKey();

        if (biome.isPresent()) {
            String biomeID = biome.get().getValue().toString();

            if (BIOME_RADIATION_VALUES.containsKey(biomeID)) {
                // Check if biome is in BIOME_RADIATION_VALUES then adds a multiplier.
                biomeMultiplier = BIOME_RADIATION_VALUES.get(biomeID).get(type);
                cache.setBiomeRadiation(type, biomeMultiplier);
            }
            // If the player is in the overworld, also apply more radiation if exposed to skylight.
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                biomeMultiplier = biomeMultiplier * (world.getLightLevel(LightType.SKY, player.getBlockPos()) / 15.0f);
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
    public static float calculateInsulators(ServerWorld world, ServerPlayerEntity player, RadiationType type, BlockPos blockPos) {
        float totalInsulation = 0.0f;
        Vec3d start = player.getPos().add(0.0, 0.1, 0.0);
        Vec3d end = blockPos.toCenterPos();

        BlockPos lastBlockPos = null;
        while (true) {
            BlockHitResult result = raycastInsulator(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player), (blockState) -> INSULATORS.containsKey(blockState.getBlock()) && INSULATORS.get(blockState.getBlock()).containsKey(type), lastBlockPos, world);
            lastBlockPos = result.getBlockPos();
            if (lastBlockPos.equals(blockPos)) break;

            RadiationEntry entry = INSULATORS.get(world.getBlockState(lastBlockPos).getBlock());
            if (entry != null) {
                totalInsulation += entry.get(type);
            }
            start = result.getPos();
        }
        return totalInsulation;
    }

    // Calculates how many radioactive blocks are within an 11x11 box from the player.
    public static float calculateBlockRadiation(ServerWorld world, ServerPlayerEntity player, RadiationType type, PlayerCache cache) {
        float radiation = 0.0f;
        BlockPos corner1 = player.getBlockPos().add(-5, -5, -5);
        BlockPos corner2 = player.getBlockPos().add(5, 5, 5);

        for (BlockPos pos : BlockPos.iterate(corner1, corner2)) {
            Block block = world.getBlockState(pos).getBlock();
            if (!block.equals(Blocks.AIR) && BLOCK_RADIATION_VALUES.containsKey(block)) {
                float insulatorValue = calculateInsulators(world, player, type, pos);
                radiation += Math.max(0, BLOCK_RADIATION_VALUES.get(block).get(type) - insulatorValue);
            }
        }
        radiation = Math.min(radiation, MAX_ITEM_INTAKE);
        cache.setBlockRadiation(type, radiation);
        return radiation;
    }

    // Calculates the amount of radiation to add to the player (for a period of time) for a type of radiation
    public static int calculateRadiationForType(ServerWorld world, ServerPlayerEntity player, RadiationType type, PlayerCache cache) {
        float biomeMultiplier = calculateBiomeRadiation(world, player, type, cache);
        float radiationAround = calculateBlockRadiation(world, player, type, cache);
        float radiationFromItems = calculateInventoryRadiation(type, cache);
        float armorProtection = 0;

        List<Item> armor = cache.createArmorMap();
        // Search through armor to find insulators.
        if (!cache.armorEquals(armor)) {
            RadiationEntry entry = RadiationEntry.createEmpty();
            for (Item item : armor) {
                ArmorInsulator insulator = ArmorInsulator.findSetForItem(item);
                // Checks if there is a valid insulator and if it can negate this type of radiation.
                if (insulator != null) {
                    insulator.radiations().entries().forEach((radiationType, value) -> entry.add(radiationType, value * insulator.getMultiplier(item) * 100));

                }
            }
            cache.updateArmor(armor, entry);
        }
        armorProtection = cache.getArmorInsulation().get(type);
        return Math.round(((radiationAround + radiationFromItems + biomeMultiplier) * (100 - Math.min(armorProtection, 100))) / DIV_CONSTANT);
    }
}
