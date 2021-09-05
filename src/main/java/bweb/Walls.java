package bweb;

import java.util.HashMap;
import bwapi.*;
import bwem.*;

public class Walls {
    HashMap<ChokePoint, Wall> walls;
    boolean logInfo = true;

    int failedPlacement = 0;
    int failedAngle = 0;
    int failedPath = 0;
    int failedTight = 0;
    int failedSpawn = 0;
    int failedPower = 0;
//
//    Wall* createWall(vector<UnitType>& buildings, const BWEM::Area * area, const BWEM::ChokePoint * choke, const UnitType tightType, const vector<UnitType>& defenses, const bool openWall, const bool requireTight)
//    {
//        ofstream writeFile;
//        string buffer;
//        auto timePointNow = chrono::system_clock::now();
//        auto timeNow = chrono::system_clock::to_time_t(timePointNow);
//
//        // Print the clock position of this Wall
//        auto clock = round((Map::getAngle(make_pair(Map::mapBWEM.Center(), Position(area->Top()))) + 90) / 30);
//        if (Position(area->Top()).x < Map::mapBWEM.Center().x)
//        clock+= 6;
//
//        // Open the log file if desired and write information
//        if (logInfo) {
//            writeFile.open("bwapi-data/write/BWEB_Wall.txt", std::ios::app);
//            writeFile << ctime(&timeNow);
//            writeFile << Broodwar->mapFileName().c_str() << endl;
//            writeFile << "At: " << clock << " o'clock." << endl;
//            writeFile << endl;
//
//            writeFile << "Buildings:" << endl;
//            for (auto &building : buildings)
//            writeFile << building.c_str() << endl;
//            writeFile << endl;
//        }
//
//        // Verify inputs are correct
//        if (!area) {
//            writeFile << "BWEB: Can't create a wall without a valid BWEM::Area" << endl;
//            return nullptr;
//        }
//
//        if (!choke) {
//            writeFile << "BWEB: Can't create a wall without a valid BWEM::Chokepoint" << endl;
//            return nullptr;
//        }
//
//        if (buildings.empty()) {
//            writeFile << "BWEB: Can't create a wall with an empty vector of UnitTypes." << endl;
//            return nullptr;
//        }
//
//        // Verify not attempting to create a Wall in the same Area/ChokePoint combination
//        for (auto &[_, wall] : walls) {
//        if (wall.getArea() == area && wall.getChokePoint() == choke) {
//            writeFile << "BWEB: Can't create a Wall where one already exists." << endl;
//            return &wall;
//        }
//    }
//
//        // Create a Wall
//        Wall wall(area, choke, buildings, defenses, tightType, requireTight, openWall);
//
//        // Verify the Wall creation was successful
//        auto wallFound = (wall.getSmallTiles().size() + wall.getMediumTiles().size() + wall.getLargeTiles().size()) == wall.getRawBuildings().size();
//
//        // Log information
//        if (logInfo) {
//            writeFile << "Failure Reasons:" << endl;
//            writeFile << "Power: " << failedPower << endl;
//            writeFile << "Angle: " << failedAngle << endl;
//            writeFile << "Placement: " << failedPlacement << endl;
//            writeFile << "Tight: " << failedTight << endl;
//            writeFile << "Path: " << failedPath << endl;
//            writeFile << "Spawn: " << failedSpawn << endl;
//            writeFile << endl;
//
//            double dur = std::chrono::duration <double, std::milli>(chrono::system_clock::now() - timePointNow).count();
//            writeFile << "Generation Time: " << dur << "ms and " << (wallFound ? "succesful." : "failed.") << endl;
//            writeFile << "--------------------" << endl;
//        }
//
//        // If we found a suitable Wall, push into container and return pointer to it
//        if (wallFound) {
//            walls.emplace(choke, wall);
//            return &walls.at(choke);
//        }
//        return nullptr;
//    }
//
//    Wall* createFFE()
//    {
//        vector<UnitType> buildings ={ UnitTypes::Protoss_Forge, UnitTypes::Protoss_Gateway, UnitTypes::Protoss_Pylon };
//        vector<UnitType> defenses(10, UnitTypes::Protoss_Photon_Cannon);
//
//        return createWall(buildings, Map::getNaturalArea(), Map::getNaturalChoke(), UnitTypes::None, defenses, true, false);
//    }
//
//    Wall* createZSimCity()
//    {
//        vector<UnitType> buildings ={ UnitTypes::Zerg_Hatchery, UnitTypes::Zerg_Evolution_Chamber };
//        vector<UnitType> defenses(10, UnitTypes::Zerg_Sunken_Colony);
//
//        return createWall(buildings, Map::getNaturalArea(), Map::getNaturalChoke(), UnitTypes::None, defenses, true, false);
//    }
//
//    Wall* createTWall()
//    {
//        vector<UnitType> buildings ={ UnitTypes::Terran_Supply_Depot, UnitTypes::Terran_Supply_Depot, UnitTypes::Terran_Barracks };
//        vector<UnitType> defenses;
//        auto type = Broodwar->enemy() && Broodwar->enemy()->getRace() == Races::Protoss ? UnitTypes::Protoss_Zealot : UnitTypes::Zerg_Zergling;
//
//        return createWall(buildings, Map::getMainArea(), Map::getMainChoke(), type, defenses, false, true);
//    }
//
//    Wall* getClosestWall(TilePosition here)
//    {
//        auto distBest = DBL_MAX;
//        Wall * bestWall = nullptr;
//        for (auto &[_, wall] : walls) {
//            const auto dist = here.getDistance(TilePosition(wall.getChokePoint()->Center()));
//
//        if (dist < distBest) {
//            distBest = dist;
//            bestWall = &wall;
//        }
//    }
//        return bestWall;
//    }
//
//    Wall* getWall(const BWEM::ChokePoint * choke)
//    {
//        if (!choke)
//            return nullptr;
//
//        for (auto &[_, wall] : walls) {
//        if (wall.getChokePoint() == choke)
//            return &wall;
//    }
//        return nullptr;
//    }
//
//    map<const BWEM::ChokePoint *, Wall>& getWalls() {
//        return walls;
//    }
//
//    void draw()
//    {
//        for (auto &[_, wall] : walls)
//        wall.draw();
//    }
}
