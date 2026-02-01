package com.doubleclickdepositworn;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Double-Click Deposit Worn Items",
        description = "Requires double-click to deposit worn items in the bank within the timed visual overlay",
        tags = {"bank", "safety", "deposit", "qol"}
)
public class DoubleDepositPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Getter
    @Inject
    private DoubleDepositConfig config;

    private DoubleDepositOverlay overlay;

    private long lastClickTime = 0;

    @Provides
    DoubleDepositConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DoubleDepositConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlay = new DoubleDepositOverlay(client, this);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlay = null;
        lastClickTime = 0;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() != MenuAction.CC_OP)
        {
            return;
        }

        Widget clicked = event.getWidget();
        if (clicked == null)
        {
            return;
        }

        Widget depositWidget = client.getWidget(
                InterfaceID.BANK,
                ComponentID.BANK_DEPOSIT_EQUIPMENT
        );

        if (depositWidget == null || clicked != depositWidget)
        {
            return;
        }

        long now = System.currentTimeMillis();

        if (now - lastClickTime > config.cooldownMillis())
        {
            event.consume();
            lastClickTime = now;
            return;
        }

        // Second click accepted
        lastClickTime = 0;
    }

    public boolean isWaitingForSecondClick()
    {
        return lastClickTime > 0
                && System.currentTimeMillis() - lastClickTime <= config.cooldownMillis();
    }

    public double getProgress()
    {
        if (!isWaitingForSecondClick())
        {
            return 0.0;
        }

        long elapsed = System.currentTimeMillis() - lastClickTime;
        return Math.min(1.0, (double) elapsed / config.cooldownMillis());
    }

    public int getOverlayOpacity()
    {
        return config.overlayOpacity();
    }
}
