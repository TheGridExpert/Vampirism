package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.config.Balance;
import de.teamlapen.vampirism.config.Configs;
import de.teamlapen.vampirism.potion.FakeNightVisionPotion;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles all events used in central parts of the mod
 */
public class ModEventHandler {
    private final static String TAG="EventHandler";
    @SubscribeEvent
    public void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent e){
        if(e.modID.equalsIgnoreCase(REFERENCE.MODID)){
            VampirismMod.log.i(TAG,"Configuration (%s) changed",e.configID);
            Configs.onConfigurationChanged();
            Balance.onConfigurationChanged();
        }
    }

    @SubscribeEvent
    public void onPlayerJoinedWorld(EntityJoinWorldEvent entityJoinWorldEvent) {
        if ((entityJoinWorldEvent.entity instanceof EntityPlayer)) {
            if (VampirismMod.proxy.isClientPlayerNull()) {
                //Did not find a better position to place this, since onPostInit is to early
                FakeNightVisionPotion.replaceNightVision();
            }
        }
    }
}