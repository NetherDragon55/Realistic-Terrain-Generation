package rtg.world.biome.realistic.biomesoplenty;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import biomesoplenty.api.biome.BOPBiomes;

import rtg.config.BiomeConfig;
import rtg.api.util.noise.CellNoise;
import rtg.util.CliffCalculator;
import rtg.api.util.noise.OpenSimplexNoise;
import rtg.world.biome.deco.DecoBaseBiomeDecorations;
import rtg.world.gen.surface.SurfaceBase;
import rtg.world.gen.terrain.TerrainBase;

public class RealisticBiomeBOPMangrove extends RealisticBiomeBOPBase {

	public static Biome biome = BOPBiomes.mangrove.get();
	public static Biome river = Biomes.RIVER;
	
	public RealisticBiomeBOPMangrove()
	{
		super(biome, river);
	}

	@Override
	public void initConfig() {}

	@Override
	public TerrainBase initTerrain() {

		return new TerrainBOPMangrove();
	}

	public class TerrainBOPMangrove extends TerrainBase
	{
		public TerrainBOPMangrove()
		{
		}

		@Override
		public float generateNoise(OpenSimplexNoise simplex, CellNoise cell, int x, int y, float border, float river)
		{
			return terrainBeach(x, y, simplex, river, 180f, 35f, 60f);
		}
	}

	@Override
	public SurfaceBase initSurface() {

		return new SurfaceBOPMangrove(config, biome.topBlock, biome.fillerBlock);
	}

	public class SurfaceBOPMangrove extends SurfaceBase {

		public SurfaceBOPMangrove(BiomeConfig config, IBlockState top, IBlockState filler) {

			super(config, top, filler);
		}

		@Override
		public void paintTerrain(ChunkPrimer primer, int i, int j, int x, int y, int depth, World world, Random rand,
								 OpenSimplexNoise simplex, CellNoise cell, float[] noise, float river, Biome[] base) {

			float c = CliffCalculator.calc(x, y, noise);
			boolean cliff = c > 1.4f ? true : false;

			for (int k = 255; k > -1; k--) {
				Block b = primer.getBlockState(x, k, y).getBlock();
				if (b == Blocks.AIR) {
					depth = -1;
				}
				else if (b == Blocks.STONE) {
					depth++;

					if (cliff && k > 64) {
						if (depth > -1 && depth < 2) {
							if (rand.nextInt(3) == 0) {

								primer.setBlockState(x, k, y, hcCobble(world, i, j, x, y, k));
							}
							else {

								primer.setBlockState(x, k, y, hcStone(world, i, j, x, y, k));
							}
						}
						else if (depth < 10) {
							primer.setBlockState(x, k, y, hcStone(world, i, j, x, y, k));
						}
					}
					else {
						if (depth == 0 && k > 61) {
							primer.setBlockState(x, k, y, topBlock);
						}
						else if (depth < 4) {
							primer.setBlockState(x, k, y, fillerBlock);
						}
					}
				}
			}
		}
	}

	@Override
	public void initDecos() {

		DecoBaseBiomeDecorations decoBaseBiomeDecorations = new DecoBaseBiomeDecorations();
		this.addDeco(decoBaseBiomeDecorations);
	}
}
