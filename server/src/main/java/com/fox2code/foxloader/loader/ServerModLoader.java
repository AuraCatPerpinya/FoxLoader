package com.fox2code.foxloader.loader;

import com.fox2code.foxloader.loader.packet.ClientHello;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.fox2code.foxloader.registry.GameRegistryServer;
import com.fox2code.foxloader.server.ServerCommandWrapper;
import com.fox2code.foxloader.server.ServerCommandWrapper4ReIndevPatches;
import com.fox2code.foxloader.updater.UpdateManager;
import net.minecraft.mitask.PlayerCommandHandler;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public final class ServerModLoader extends ModLoader {
    public static void launchModdedServer(String... args) {
        ModLoader.foxLoader.serverMod = new ServerModLoader();
        ModLoader.foxLoader.serverMod.modContainer = ModLoader.foxLoader;
        Objects.requireNonNull(ModLoader.foxLoader.getMod(), "WTF???");
        ModLoader.initializeModdedInstance(false);
        ServerSelfTest.selfTest();
        MinecraftServer.main(args);
    }

    public static void notifyNetworkPlayerJoined(NetworkPlayer networkPlayer) {
        for (ModContainer modContainer : ModLoader.modContainers.values()) {
            modContainer.notifyNetworkPlayerJoined(networkPlayer);
        }
    }

    private ServerModLoader() {}

    public static class Internal {
        public static void notifyRun() {
            GameRegistryServer.initialize();
            ModLoader.initializeMods(false);
            UpdateManager.getInstance().initialize();
            GameRegistryServer.freeze();
            ModLoader.postInitializeMods();
            GameRegistryServer.freezeRecipes();
            // StatList.initBreakableStats();
            // StatList.initStats();
            boolean hasReIndevPatches;
            try {
                //noinspection ResultOfMethodCallIgnored
                PlayerCommandHandler.commands.getClass();
                hasReIndevPatches = false;
            } catch (Throwable t) {
                hasReIndevPatches = true;
            }
            if (hasReIndevPatches) {
                ServerCommandWrapper4ReIndevPatches.registerAllForReIndevPatches();
            } else {
                for (CommandCompat commandCompat : CommandCompat.commands.values()) {
                    PlayerCommandHandler.commands.add(new ServerCommandWrapper(commandCompat));
                }
            }
        }
    }

    @Override
    public void onReceiveClientPacket(NetworkPlayer networkPlayer, byte[] data) {
        if (data.length == 0) return;
        LoaderNetworkManager.executeClientPacketData(networkPlayer, data);
    }

    @Override
    void loaderHandleClientHello(NetworkPlayer networkPlayer, ClientHello clientHello) {
        for (ModContainer modContainer : ModLoader.modContainers.values()) {
            modContainer.notifyNetworkPlayerHello(networkPlayer, clientHello);
        }
    }
}
