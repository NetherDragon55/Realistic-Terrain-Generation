package teamrtg.rtg.world.biome.fake;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderOverworld;
import teamrtg.rtg.api.mods.Mods;
import teamrtg.rtg.api.util.BiomeUtils;
import teamrtg.rtg.util.LimitedMap;
import teamrtg.rtg.util.PlaneLocation;
import teamrtg.rtg.world.gen.ChunkProviderRTG;
import teamrtg.rtg.world.gen.RealisticBiomeGenerator;

import static teamrtg.rtg.util.math.MathUtils.globalToChunk;
import static teamrtg.rtg.util.math.MathUtils.globalToLocal;

/**
 * @author topisani
 */
public class RealisticBiomeFaker {

    private final ChunkProviderOverworld fakeProvider;
    public boolean[] fakeBiomes = new boolean[256];
    private LimitedMap<PlaneLocation.Invariant, int[]> chunkHeights = new LimitedMap<>(64); //Keep the heights for the last 64 chunks around for a bit. We might need them
    private ChunkProviderRTG chunkProvider;

    public RealisticBiomeFaker(ChunkProviderRTG chunkProvider) {
        this.chunkProvider = chunkProvider;
        fakeProvider = new ChunkProviderOverworld(chunkProvider.world, chunkProvider.world.getSeed(), chunkProvider.world.getWorldInfo().isMapFeaturesEnabled(), chunkProvider.world.getWorldInfo().getGeneratorOptions());
    }

    public int[] fakeTerrain(int cx, int cz) {
        ChunkPrimer primer = new ChunkPrimer();
        fakeProvider.setBlocksInChunk(cx, cz, primer);
        int[] heights = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 255; y >= 0; --y) {
                    IBlockState iblockstate = primer.getBlockState(x, y, z);
                    if (iblockstate != null && iblockstate != Blocks.AIR.getDefaultState()) {
                        heights[x * 16 + z] = y;
                        break;
                    }
                }
            }
        }
        chunkHeights.put(new PlaneLocation.Invariant(cx, cz), heights);
        return heights;
    }

    public boolean isFakeBiome(int id) {
        return fakeBiomes[id];
    }

    public int getHeightAt(int x, int z) {
        return this.getHeightsAt(globalToChunk(x), globalToChunk(z))[globalToLocal(x) * 16 + globalToLocal(z)];
    }

    public int[] getHeightsAt(int cx, int cz) {
        PlaneLocation.Invariant inLoc = new PlaneLocation.Invariant(cx, cz);
        int[] heights = null;
        for (PlaneLocation.Invariant location : chunkHeights.keySet()) {
            if (location.equals(inLoc)) heights = chunkHeights.get(location);
        }
        if (heights == null) heights = this.fakeTerrain(cx, cz);
        return heights;
    }

    public void fakeSurface(int cx, int cz, ChunkPrimer primer, BiomeGenBase[] biomes) {
        fakeProvider.replaceBiomeBlocks(cx, cz, primer, biomes);
    }

    public void initFakeBiomes() {
        BiomeGenBase[] b = BiomeUtils.getRegisteredBiomes();
        for (BiomeGenBase biome : b) {
            if (biome != null) {
                try {
                    RealisticBiomeGenerator.getBiome(BiomeGenBase.getIdForBiome(biome));
                } catch (Exception e) {
                    new RealisticBiomeGenerator(new FakedRealisticBiome(Mods.RTG, biome, chunkProvider));
                    fakeBiomes[BiomeGenBase.getIdForBiome(biome)] = true;
                }
            }
        }
    }
}