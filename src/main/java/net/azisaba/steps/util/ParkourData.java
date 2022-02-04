package net.azisaba.steps.util;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.azisaba.steps.light.LightEngine;
import net.azisaba.steps.worldgenerator.VoidParkourWorldGenerator;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

@Getter
@RequiredArgsConstructor
public class ParkourData {

  private final Player player;
  private Instance instance;
  private ForwardBlockGenerator blockGenerator;

  private boolean died = false;

  private double record;

  public void initPlayer() {
    record = 0;
    died = false;
    if (instance == null) {
      instance = MinecraftServer.getInstanceManager().createInstanceContainer();
      instance.setChunkGenerator(VoidParkourWorldGenerator.GENERATOR);
      instance.setTimeRate(0);
      instance.enableAutoChunkLoad(false);
      instance.loadChunk(0, 0);
    }
    if (blockGenerator != null) {
      blockGenerator.reset();
    } else {
      blockGenerator = new ForwardBlockGenerator(instance);
    }

    Pos start = new Pos(5, 5, 8, -90, 0);
    player.setRespawnPoint(new Pos(5, 5, 8, -90, 0));
    if (player.getInstance() != null) {
      Chunk c = instance.getChunkAt(start.x(), start.z());
      if (c == null || !c.isLoaded()) {
        instance.loadChunk(start);
      }
      TeleportUtils.teleport(player, start);
      player.setGameMode(GameMode.SURVIVAL);
    }

    MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
      LightEngine engine = new LightEngine();
      engine.recalculateInstance(instance);
    });

    MinecraftServer.getSchedulerManager().buildTask(() -> {
      try {
        for (int i = 0; i < 10; i++) {
          blockGenerator.generateForward();
        }
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }).delay(Duration.ofMillis(100)).schedule();
  }

  public void markAsDied() {
    died = true;
    record = player.getPosition().x() - 10;
    if (record <= 0) {
      record = 0;
    }

    player.setGameMode(GameMode.SPECTATOR);
    player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Source.MASTER, 1f, 1f));
    player.showTitle(
        Title.title(generateRecordTitleComponent(record), generateSubTitle(),
            Times.of(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1))));

    MinecraftServer.getSchedulerManager().buildTask(() -> {
      if (!player.isOnline()) {
        return;
      }

      MinecraftServer.getSchedulerManager().scheduleNextTick(this::initPlayer);
    }).delay(Duration.ofSeconds(5)).schedule();
  }

  private Component generateRecordTitleComponent(double record) {
    Component yourRecord = Component.text("記録: ").color(NamedTextColor.GRAY);
    Component recordSection = Component.text(String.format("%.2f", record) + "m")
        .color(NamedTextColor.YELLOW);
    return yourRecord.append(recordSection);
  }

  private Component generateSubTitle() {
    return Component.text("5秒後にTPします...").color(NamedTextColor.GRAY);
  }
}
