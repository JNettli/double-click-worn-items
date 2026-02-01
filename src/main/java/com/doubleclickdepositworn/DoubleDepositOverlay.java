package com.doubleclickdepositworn;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetInfo;
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
        if (!plugin.isWaitingForSecondClick())
            return null;

        var widget = client.getWidget(WidgetInfo.BANK_DEPOSIT_EQUIPMENT);
        if (widget == null || widget.isHidden())
            return null;

        Rectangle bounds = widget.getBounds();

        graphics.setColor(new Color(0, 0, 0, 50));
        graphics.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);

        Color baseColor = plugin.getConfig().progressBarColor();
        int alpha = plugin.getOverlayOpacity();
        Color progressColor = new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                alpha
        );

        graphics.setColor(progressColor);

        double progress = plugin.getProgress();
        int startAngle = 90;
        int arcAngle = -(int) (360 * progress);
        graphics.fillArc(bounds.x, bounds.y, bounds.width, bounds.height, startAngle, arcAngle);

        return null;
    }
}
