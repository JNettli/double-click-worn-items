package com.doubleclickdepositworn;

import com.google.inject.Provides;
import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Double-Click Deposit Worn Items",
        description = "Requires double-click to deposit worn items in the bank within the timed visual overlay",
        tags = {"bank", "safety", "deposit"}
)
public class DoubleDepositPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

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
    protected void startUp() throws Exception
    {
        overlay = new DoubleDepositOverlay(client, this);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        overlay = null;
        lastClickTime = 0;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() != MenuAction.CC_OP)
            return;

        Widget clicked = event.getWidget();
        if (clicked == null)
            return;

        Widget depositWidget = client.getWidget(WidgetInfo.BANK_DEPOSIT_EQUIPMENT);
        if (depositWidget == null || clicked != depositWidget)
            return;

        long now = System.currentTimeMillis();

        if (now - lastClickTime > config.cooldownMillis())
        {
            event.consume();
            lastClickTime = now;
            return;
        }

        lastClickTime = 0;
    }

    public boolean isWaitingForSecondClick()
    {
        return lastClickTime > 0 && (System.currentTimeMillis() - lastClickTime <= config.cooldownMillis());
    }

    public double getProgress()
    {
        if (!isWaitingForSecondClick())
            return 0.0;

        long elapsed = System.currentTimeMillis() - lastClickTime;
        return Math.min(1.0, (double) elapsed / config.cooldownMillis());
    }

    public DoubleDepositConfig getConfig()
    {
        return config;
    }

    public int getOverlayOpacity()
    {
        return config.overlayOpacity();
    }

}
