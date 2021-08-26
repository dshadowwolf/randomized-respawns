package com.mcmoddev.randomspawns;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RandomSpawnsConfig {
	static {
		final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	private RandomSpawnsConfig() {

	}

	public static final ForgeConfigSpec COMMON_SPEC;
	public static final CommonConfig COMMON;

	public static class CommonConfig {
		public final ForgeConfigSpec.IntValue randomSpawnRadius;

		CommonConfig(ForgeConfigSpec.Builder builder) {
			builder.push("general");
			randomSpawnRadius = builder
				.comment("Radius from world spawn that players will randomly spawn in")
				.defineInRange("radius", 500, 0, 2000);
			builder.pop();
		}
	}
}
