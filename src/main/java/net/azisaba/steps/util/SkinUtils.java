package net.azisaba.steps.util;

import java.util.concurrent.ConcurrentHashMap;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;

public class SkinUtils {

  private final ConcurrentHashMap<String, PlayerSkin> skinCache = new ConcurrentHashMap<>();

  public void apply(Player player) {
    PlayerSkin.fromUsername(player.getName().toString());
  }

}
