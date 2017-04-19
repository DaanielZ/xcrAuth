package pl.xcrafters.xcrauth;

import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaRenderer extends MapRenderer {

    AuthPlugin plugin;

    public CaptchaRenderer(AuthPlugin plugin) {
        super(true);
        this.plugin = plugin;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if(!plugin.notRendered.contains(player.getName())) {
            return;
        }
        plugin.notRendered.remove(player.getName());

        MapCursorCollection mcc = canvas.getCursors();
        for(int i = 0; i < mcc.size(); i++) {
            mcc.removeCursor(mcc.getCursor(i));
        }
        canvas.setCursors(mcc);

        Random random = new Random();
        map.setScale(MapView.Scale.NORMAL);

        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 128, 128);
        GradientPaint gradientPaint = new GradientPaint(
                0, 0, new Color(0xCCCCCC),
                128, 128, new Color(0x999999)
        );
        graphics.setPaint(gradientPaint);
        graphics.fillRect(0, 0, 128, 128);
        graphics.dispose();

        canvas.drawImage(0, 0, image);

        String captcha = plugin.captchas.get(player.getName());
        int x = 16;
        for(int i=0; i<captcha.length(); i++) {
            int y = 56 + random.nextInt(16);
            char ch = captcha.charAt(i);
            canvas.drawText(x, y, MinecraftFont.Font, Character.toString(ch));
            x += 10 + random.nextInt(5);
        }
        player.sendMap(map);
    }

    @Override
    public void initialize(MapView map) {
    }

}
