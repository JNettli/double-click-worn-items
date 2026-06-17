package com.doubleclickdepositworn;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;

public class DoubleDepositOverlay extends Overlay
{
    private final Client client;
    private final DoubleDepositPlugin plugin;

    @Inject
    public DoubleDepositOverlay(Client client, DoubleDepositPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(1000.0F);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isOverlayVisible())
        {
            return null;
        }

        Widget widget = plugin.getLastDepositWornWidget();
        if (widget == null)
        {
            return null;
        }

        Rectangle bounds = widget.getBounds();
        if (bounds == null)
        {
            return null;
        }

        int centerX = bounds.x + bounds.width / 2;

        int centerY = bounds.y + bounds.height / 2;

        int size = (int) (Math.min(bounds.width, bounds.height) * 0.9);

        graphics.setColor(new Color(0, 0, 0, 50));
        graphics.fillOval(centerX - size / 2, centerY - size / 2, size, size);

        Color c = plugin.getConfig().progressBarColor();
        int alpha = plugin.getOverlayOpacity();

        graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));

        double progress = plugin.getProgress();
        int arc = -(int) (360 * progress);

        graphics.fillArc(
                centerX - size / 2,
                centerY - size / 2,
                size,
                size,
                90,
                arc
        );

        return null;
    }
}