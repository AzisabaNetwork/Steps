package net.azisaba.steps;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.azisaba.steps.util.ForwardBlockGenerator;
import net.azisaba.steps.util.ParkourData;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerChunkUnloadEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;

public class Steps {

  private static final ConcurrentHashMap<UUID, ParkourData> dataMap = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    MinecraftServer minecraftServer = MinecraftServer.init();
    GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

    // register Events
    registerEvents(globalEventHandler);
    // run actionbar task
    MinecraftServer.getSchedulerManager().buildTask(() -> {
      MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
        double x = p.getPosition().x();
        if (x < 10) {
          return;
        }
        if (dataMap.get(p.getUuid()) == null || dataMap.get(p.getUuid()).isDied()) {
          return;
        }

        p.sendActionBar(
            Component.text(String.format("%.2f", (x - 10d)) + "m").color(NamedTextColor.YELLOW));
      });
    }).repeat(Duration.ofMillis(200)).schedule();

    BungeeCordProxy.enable();

    minecraftServer.start("0.0.0.0", 25565);
  }

  private static void registerEvents(GlobalEventHandler eventHandler) {
    eventHandler.addListener(PlayerLoginEvent.class, event -> {
      final Player player = event.getPlayer();

      ParkourData data = new ParkourData(player);
      data.initPlayer();
      event.setSpawningInstance(data.getInstance());
      dataMap.put(player.getUuid(), data);

      MinecraftServer.getSchedulerManager()
          .scheduleNextTick(() -> player.teleport(new Pos(5, 5, 8, -90, 0)));
    });

    eventHandler.addListener(PlayerMoveEvent.class, event -> {
      Player p = event.getPlayer();
      if (!dataMap.containsKey(p.getUuid()) || p.getInstance() == null) {
        return;
      }

      ParkourData data = dataMap.get(p.getUuid());
      if (data.isDied()) {
        return;
      }
      ForwardBlockGenerator generator = data.getBlockGenerator();

      TreeSet<Pos> posSet = generator.getPosSet();
      posSet.stream()
          .filter((pos) -> pos.blockX() + 5 < p.getPosition().x())
          .forEach(
              (pos) -> {
                Instance instance = p.getInstance();
                if (instance.getBlock(pos.blockX(), pos.blockY(), pos.blockZ()) == Block.STONE) {
                  return;
                }
                instance.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.AIR);
                instance.playSound(
                    Sound.sound(SoundEvent.BLOCK_STONE_BREAK, Source.BLOCK, 1f, 1f),
                    pos.blockX(), pos.blockY(), pos.blockZ());
              }
          );
      posSet.removeIf((pos) -> pos.blockX() + 5 < p.getPosition().x());

      for (int i = posSet.size(); i < 10; i++) {
        try {
          generator.generateForward();
        } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
        }
      }
    });

    eventHandler.addListener(PlayerMoveEvent.class, (event) -> {
      if (event.getNewPosition().y() >= 0) {
        return;
      }

      ParkourData data = dataMap.get(event.getPlayer().getUuid());
      if (!data.isDied()) {
        dataMap.get(event.getPlayer().getUuid()).markAsDied();
      }
    });

    eventHandler.addListener(PlayerDisconnectEvent.class, (event) -> {
      Player p = event.getPlayer();
      dataMap.remove(p.getUuid());

      Instance instance = p.getInstance();
      if (instance == null) {
        return;
      }

      MinecraftServer.getSchedulerManager().scheduleNextProcess(
          () -> MinecraftServer.getInstanceManager().unregisterInstance(instance)
      );
    });

    eventHandler.addListener(EntityDamageEvent.class, (event) -> {
      if (event.getEntity() instanceof Player) {
        event.setCancelled(true);
      }
    });

    eventHandler.addListener(PlayerChunkUnloadEvent.class, (event) -> {
      Instance instance = event.getPlayer().getInstance();
      if (instance == null) {
        return;
      }
      if (event.getChunkX() >= 0 && event.getChunkZ() == 0) {
        return;
      }
      Chunk chunk = instance.getChunk(event.getChunkX(), event.getChunkZ());
      if (chunk != null && chunk.isLoaded()) {
        instance.unloadChunk(event.getChunkX(), event.getChunkZ());
      }
    });

    eventHandler.addListener(PlayerChatEvent.class, (event) -> event.setCancelled(true));
    eventHandler.addListener(PlayerBlockPlaceEvent.class, (event) -> event.setCancelled(true));
    eventHandler.addListener(PlayerBlockBreakEvent.class, (event) -> event.setCancelled(true));

    MinecraftServer.getSchedulerManager().buildShutdownTask(
        () -> MinecraftServer.getConnectionManager().getOnlinePlayers()
            .forEach(
                player -> player.kick(Component.text("Server is shutting down"))
            )
    );
  }
}
