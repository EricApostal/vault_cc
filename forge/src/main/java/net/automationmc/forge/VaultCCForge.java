package net.automationmc.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.automationmc.VaultCC;

@Mod(VaultCC.MOD_ID)
public final class VaultCCForge {
    public VaultCCForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VaultCC.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());

        // Run our common setup.
        VaultCC.init();
    }

    public static class ServerEventHandler {
        @SubscribeEvent
        public void onServerStarted(ServerStartedEvent event) {
            VaultCC.onServerStarted();
        }
    }
}
