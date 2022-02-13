package net.azisaba.steps.util;

import java.util.concurrent.CompletableFuture;
import net.azisaba.steps.light.LightEngine;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class TeleportUtils {

  private static final LightEngine engine = new LightEngine();

  public static CompletableFuture<Void> teleport(Player player, Pos pos) {
    Instance instance = player.getInstance();
    if (instance == null) {
      throw new IllegalArgumentException();
    }

    return instance.loadChunk(pos)
        .thenAccept((chunk) -> player.teleport(pos))
        .thenAccept((unused) -> engine.recalculateInstance(player.getInstance()));
  }

  public static CompletableFuture<Void> teleport(Player player, Vec vec) {
    return teleport(player, vec.asPosition());
  }
}
