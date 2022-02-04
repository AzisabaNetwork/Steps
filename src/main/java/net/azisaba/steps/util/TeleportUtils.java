package net.azisaba.steps.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class TeleportUtils {

  public static void teleport(Player player, Pos pos) {
    Instance instance = player.getInstance();
    if (instance == null) {
      throw new IllegalArgumentException();
    }

    if (!instance.isChunkLoaded(pos)) {
      instance.loadChunk(pos);
    }
    player.teleport(pos);
  }

  public static void teleport(Player player, Vec vec) {
    teleport(player, vec.asPosition());
  }
}
