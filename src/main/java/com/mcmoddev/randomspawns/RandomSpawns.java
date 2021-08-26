package com.mcmoddev.randomspawns;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Random;

/**
 *
 */
@Mod("randomspawns")
public class RandomSpawns {

	/**
	 * Directly reference a log4j logger.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 *
	 */
	public RandomSpawns() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RandomSpawnsConfig.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static BlockPos findValidSpawnPos(ServerWorld worldIn, BlockPos base) {
		Biome biome = worldIn.getBiome(base);
		BlockState bs = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
		boolean flag = worldIn.dimensionType().hasCeiling();

		return getRespawnPos(worldIn, base.getX(), base.getZ(), flag, bs);
	}

	@Nullable
	private static BlockPos getRespawnPos(ServerWorld worldIn, int xPos, int zPos, boolean hasCeiling, BlockState surfState) {
		Chunk chunk = worldIn.getChunk(xPos >> 4, zPos >> 4);
		int xW = xPos & 15;
		int zW = zPos & 15;
		int max = hasCeiling ? worldIn.getChunkSource().getGenerator().getSpawnHeight() : chunk.getHeight(Heightmap.Type.MOTION_BLOCKING, xW, zW);
		if (max < 0) {
			return null;
		} else {
			int surfaceHeight = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, xW, zW);
			int oceanFloor = chunk.getHeight(Heightmap.Type.OCEAN_FLOOR, xW, zW);
			if (surfaceHeight < max && surfaceHeight > oceanFloor) {
				return null;
			} else {
				return seekSurface(worldIn, max, xPos, zPos, surfState);
			}
		}
	}

	private static BlockPos seekSurface(ServerWorld worldIn, int max, int xPos, int zPos, BlockState surfaceState) {
		BlockPos.Mutable retVal = new BlockPos.Mutable(xPos, 0, zPos);
		for (int curr = max + 1; curr >= 0; --curr) {
			retVal.set(xPos, curr, zPos);
			BlockState tState = worldIn.getBlockState(retVal);
			if (!tState.getFluidState().isEmpty()) break;

			if (tState.equals(surfaceState)) return retVal.above().immutable();
		}

		return null;
	}

	@SubscribeEvent
	public void respawning(final PlayerEvent.PlayerRespawnEvent event) {
		int radius = RandomSpawnsConfig.COMMON.randomSpawnRadius.get();

		if (radius == 0) {
			return;
		}

		if (event.getPlayer().getCommandSenderWorld().isClientSide()) return;
		ServerPlayerEntity pl = (ServerPlayerEntity) event.getPlayer();
		ServerWorld w = (ServerWorld) pl.getCommandSenderWorld();
		BlockPos z = w.getSharedSpawnPos();
		BlockPos k = pl.getRespawnPosition();

		if (k == null || z.equals(k)) {
			Random r = w.getRandom();
			BlockPos rPos = null;
			int cc = 0;
			while (rPos == null && ++cc < 15) {
				int nx = z.getX() + r.nextInt(radius);
				int nz = z.getZ() + r.nextInt(radius);
				BlockPos pt = new BlockPos(nx, z.getY(), nz);
				rPos = findValidSpawnPos(w, pt);
			}
			if ( cc >= 15 || rPos == null ) return;
			pl.teleportTo(w, rPos.getX(), rPos.getY(), rPos.getZ(), w.getSharedSpawnAngle(), 0);
			pl.displayClientMessage(new StringTextComponent(String.format("Respawned at %s", rPos)), false);
		}
	}
}
