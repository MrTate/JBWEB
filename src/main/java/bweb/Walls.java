package bweb;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bwapi.*;
import bwem.*;

public class Walls {
    static HashMap<ChokePoint, Wall> walls;
    boolean logInfo = true;

    int failedPlacement = 0;
    int failedAngle = 0;
    int failedPath = 0;
    int failedTight = 0;
    int failedSpawn = 0;
    int failedPower = 0;

    Wall createWall(List<UnitType> buildings, Area area, ChokePoint choke, UnitType tightType, List<UnitType> defenses, boolean openWall, boolean requireTight) {
        FileWriter writeFile = null;
        try {
            writeFile = new FileWriter("bwapi-data/write/BWEB_Wall.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String timeNow = formatter.format(date);

        // Print the clock position of this Wall
        double clock = Math.round((Map.getAngle(new Pair<>(Map.mapBWEM.getMap().getCenter(), new Position(area.getTop()))) + 90) / 30);
        if (new Position(area.getTop()).x < Map.mapBWEM.getMap().getCenter().x) {
            clock += 6;
        }

        // Open the log file if desired and write information
        if (logInfo) {
            try {
                writeFile.write(timeNow);
                writeFile.write(Map.game.mapFileName());
                writeFile.write("At: " + clock + " o'clock.");
                writeFile.write("\n");
                writeFile.write("Buildings:");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (UnitType building : buildings){
                try {
                    writeFile.write(building.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Verify inputs are correct
        if (area == null) {
            try {
                writeFile.write("JBWEB: Can't create a wall without a valid Area");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        if (choke == null) {

            try {
                writeFile.write("JBWEB: Can't create a wall without a valid Chokepoint");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        if (buildings.isEmpty()) {
            try {
                writeFile.write("JBWEB: Can't create a wall with an empty vector of UnitTypes.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // Verify not attempting to create a Wall in the same Area/ChokePoint combination
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            if (wall.getArea() == area && wall.getChokePoint() == choke) {
                try {
                    writeFile.write("JBWEB: Can't create a Wall where one already exists.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return wall;
            }
        }

        // Create a Wall
        Wall wall = new Wall(area, choke, buildings, defenses, tightType, requireTight, openWall);

        // Verify the Wall creation was successful
        boolean wallFound = (wall.getSmallTiles().size() + wall.getMediumTiles().size() + wall.getLargeTiles().size()) == wall.getRawBuildings().size();

        // Log information
        if (logInfo) {
            try {
                writeFile.write("Failure Reasons:");
                writeFile.write("Power: " + failedPower);
                writeFile.write("Angle: " + failedAngle);
                writeFile.write("Placement: " + failedPlacement);
                writeFile.write("Tight: " + failedTight);
                writeFile.write("Path: " + failedPath);
                writeFile.write("Spawn: " + failedSpawn);
                writeFile.write("\n");

                date = new Date(System.currentTimeMillis() - date.getTime());
                writeFile.write("Generation Time: " + date.getTime() + "ms and " + (wallFound ? "successful." : "failed."));
                writeFile.write("--------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If we found a suitable Wall, push into container and return pointer to it
        if (wallFound) {
            walls.replace(choke, wall);
            return walls.get(choke);
        }

        return null;
    }

    Wall createFFE() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Protoss_Forge);
        buildings.add(UnitType.Protoss_Gateway);
        buildings.add(UnitType.Protoss_Pylon);
        List<UnitType> defenses = new ArrayList<>();
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        defenses.add(UnitType.Protoss_Photon_Cannon);
        return createWall(buildings, Map.getNaturalArea(), Map.getNaturalChoke(), UnitType.None, defenses, true, false);
    }

    Wall createZSimCity() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Zerg_Hatchery);
        buildings.add(UnitType.Zerg_Evolution_Chamber);
        List<UnitType> defenses = new ArrayList<>();
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        defenses.add(UnitType.Zerg_Sunken_Colony);
        return createWall(buildings, Map.getNaturalArea(), Map.getNaturalChoke(), UnitType.None, defenses, true, false);
    }

    Wall createTWall() {
        List<UnitType> buildings = new ArrayList<>();
        buildings.add(UnitType.Terran_Supply_Depot);
        buildings.add(UnitType.Terran_Supply_Depot);
        buildings.add(UnitType.Terran_Barracks);
        List<UnitType> defenses = new ArrayList<>();
        UnitType type = Map.game.enemy() != null && Map.game.enemy().getRace() == Race.Protoss ? UnitType.Protoss_Zealot : UnitType.Zerg_Zergling;

        return createWall(buildings, Map.getMainArea(), Map.getMainChoke(), type, defenses, false, true);
    }

    Wall getClosestWall(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Wall bestWall = null;
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            double dist = here.getDistance(new TilePosition(wall.getChokePoint().getCenter()));

        if (dist < distBest) {
            distBest = dist;
            bestWall = wall;
        }
    }
        return bestWall;
    }

    Wall getWall(ChokePoint choke) {
        if (choke == null) {
            return null;
        }

        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            if (wall.getChokePoint() == choke) {
                return wall;
            }
        }
        return null;
    }

    public static HashMap<ChokePoint, Wall> getWalls() {
        return walls;
    }

    void draw() {
        for (ChokePoint chokePoint : walls.keySet()) {
            Wall wall = walls.get(chokePoint);
            wall.draw();
        }
    }
}
