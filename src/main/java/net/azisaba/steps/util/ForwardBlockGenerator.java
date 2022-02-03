package net.azisaba.steps.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

@RequiredArgsConstructor
public class ForwardBlockGenerator {

  private final Instance instance;

  @Getter
  private final TreeSet<Pos> posSet = new TreeSet<>(
      Comparator.comparingInt(Point::blockX).reversed());

  private final List<Block> blocks = Block.values().stream()
      .filter(t -> t.name().endsWith("_concrete"))
      .toList();
  private SecureRandom random = null;

  public void generateForward() throws NoSuchAlgorithmException {
    if (posSet.isEmpty()) {
      Pos first = new Pos(11, 4, 8);
      posSet.add(first);
      instance.setBlock(first.blockX(), first.blockY(), first.blockZ(), getRandomBlock());
    }
    Pos pos = posSet.first();
    Vec from = pos.asVec();

    Vec move = null;
    while (move == null || move.length() >= 5) {
      move = getNextVec(from);

      Vec added = from.add(move);
      if (added.y() < 0 || added.y() > 100) {
        move = null;
      }
    }

    Vec next = from.add(move);
    instance.setBlock(next.blockX(), next.blockY(), next.blockZ(), getRandomBlock());
    posSet.add(next.asPosition());
  }

  public void reset() {
    posSet.forEach(pos -> instance.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.AIR));
    posSet.clear();
  }

  private Vec getNextVec(Vec from) throws NoSuchAlgorithmException {
    if (random == null) {
      random = SecureRandom.getInstance("SHA1PRNG");
    }

    int x = random.nextInt(3) + 4; // 4 to 6
    int y = random.nextInt(3) - 1; // -1 to 1
    int z = random.nextInt(3) - 1; // -1 to 1

    return new Vec(x, y, z);
  }

  private Block getRandomBlock() {
    if (random == null) {
      return blocks.get(0);
    }
    return blocks.get(random.nextInt(blocks.size()));
  }
}
