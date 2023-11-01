package nurgling.bots.actions;

import haven.Coord;
import haven.Coord2d;
import haven.GItem;
import nurgling.*;

import static haven.OCache.posres;

import java.awt.Color;

public class WaterQMap implements Action {

    @Override
    public Results run(NGameUI gui)
            throws InterruptedException {
        // while (true) {

        int x_tiles = 6;
        int y_tiles = 6;
        // playerMoveNTiles(gui, x_tiles, y_tiles);
        getWaterQ(gui);
        // Thread.sleep(1000);
        // }
        return new Results(Results.Types.SUCCESS);
    }

    public void playerMoveNTiles(NGameUI gui, int x_tiles, int y_tiles) {
        Coord2d player_coord = gui.getMap().player().rc;
        Coord2d in_tile = Coord2d.of(player_coord.x % 11, player_coord.y % 11);

        Coord2d center_adjustment = Coord2d.of(-5.5 - in_tile.x, -5.5 - in_tile.y);
        Coord2d next_coord = player_coord.add(x_tiles * 11 + center_adjustment.x, y_tiles * 11 + center_adjustment.y);
        gui.map.wdgmsg("click", Coord.z, next_coord.floor(posres), 1, 0);
    }

    public WaterQMap() {
    }

    private void GrabWaterIntoCup(NGameUI gui) throws InterruptedException {
        GItem item = gui.getInventory().getItem(new NAlias("woodencup"));
        new TakeToHand(item).run(gui);
        gui.map.wdgmsg("itemact", Coord.z, gui.getMap().player().rc.floor(posres), 0);
        Thread.sleep(500);
        NUtils.transferToInventory();
    }

    private void CleanupCup(NGameUI gui) throws InterruptedException {
        gui.map.wdgmsg("click", Coord.z, gui.getMap().player().rc.floor(posres), 3, 0, 0);
        NUtils.waitEvent(() -> gui.getInventory().getItem(new NAlias("woodencup")) != null, 60);
        new SelectFlowerAction((NGItem) gui.getInventory().getItem(new NAlias("woodencup")), "Empty",
                SelectFlowerAction.Types.Item).run(gui);
        NUtils.waitEvent(() -> NUtils.getContent(gui.getInventory().getItem(new NAlias("woodencup"))) == null, 10);
    }

    private double QualityOfWaterInCup(NGameUI gui) throws InterruptedException {
        return NUtils.getContentQuality(gui.getInventory().getItem(new NAlias("woodencup")));
    }

    private void getWaterQ(NGameUI gui) throws InterruptedException {
        GrabWaterIntoCup(gui);
        NotifyUserAndMarkWaterQ(gui, QualityOfWaterInCup(gui));
        CleanupCup(gui);
    }

    private Color colorForWaterQ(NGameUI gui, double water_q) {
        int multiplier = 50 + 10 * ((int) water_q % 20);
        if (water_q >= 0 && water_q < 20) {
            return new Color(multiplier, multiplier, multiplier);
        }
        if (water_q >= 20 && water_q < 40) {
            return new Color(0, multiplier, 0);
        }
        if (water_q >= 40 && water_q < 60) {
            return new Color(0, 0, multiplier);
        }
        if (water_q >= 60 && water_q < 80) {
            return new Color(multiplier, 0, 0);
        }
        if (water_q >= 80 && water_q < 100) {
            return new Color(multiplier, multiplier, 0);
        }
        if (water_q >= 100 && water_q < 120) {
            return new Color(0, multiplier, multiplier);
        }

        if (water_q >= 120 && water_q < 140) {
            return new Color(multiplier, 0, multiplier);
        }
        return new Color(255, multiplier, 0);
    }

    private void NotifyUserAndMarkWaterQ(NGameUI gui, double water_q) {
        String message = "Water q " + String.valueOf(water_q);
        gui.msg(message);
        gui.mapfile.addMarkerAtPlayer(message, colorForWaterQ(gui, water_q));
    }
}