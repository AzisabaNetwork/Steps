package net.azisaba.steps.worldgenerator;

import java.util.List;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoidParkourWorldGenerator implements ChunkGenerator {

  public static final VoidParkourWorldGenerator GENERATOR = new VoidParkourWorldGenerator();

  @Override
  public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
    if (chunkX != 0 || chunkZ != 0) {
      return;
    }

    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 5; y++) {
        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
          batch.setBlock(x, y, z, Block.STONE);
        }
      }
    }
  }

  @Override
  public @Nullable List<ChunkPopulator> getPopulators() {
    return null;
  }
}
