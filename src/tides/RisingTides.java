package tides;

import java.util.*;

/**
 * This class contains methods that provide information about select terrains 
 * using 2D arrays. Uses floodfill to flood given maps and uses that 
 * information to understand the potential impacts. 
 * Instance Variables:
 *  - a double array for all the heights for each cell
 *  - a GridLocation array for the sources of water on empty terrain 
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 * @author Vian Miranda (Rutgers University)
 */
public class RisingTides {

    // Instance variables
    private double[][] terrain;     // an array for all the heights for each cell
    private GridLocation[] sources; // an array for the sources of water on empty terrain 

    /**
     * DO NOT EDIT!
     * Constructor for RisingTides.
     * @param terrain passes in the selected terrain 
     */
    public RisingTides(Terrain terrain) {
        this.terrain = terrain.heights;
        this.sources = terrain.sources;
    }

    /**
     * Find the lowest and highest point of the terrain and output it.
     * 
     * @return double[][], with index 0 and index 1 being the lowest and 
     * highest points of the terrain, respectively
     */
    public double[] elevationExtrema() {

        if (terrain == null || terrain.length == 0 || terrain[0].length == 0) {
            return null; 
        }
    
        double lowest = Double.MAX_VALUE;
        double highest = Double.MIN_VALUE;
    
        for (int row = 0; row < terrain.length; row++) {
            for (int col = 0; col < terrain[0].length; col++) {
                double currentHeight = terrain[row][col];
                if (currentHeight < lowest) {
                    lowest = currentHeight;
                }
                if (currentHeight > highest) {
                    highest = currentHeight;
                }
            }
        }
    
        double[] result = {lowest, highest};
        return result;
    }

    /**
     * Implement the floodfill algorithm using the provided terrain and sources.
     * 
     * All water originates from the source GridLocation. If the height of the 
     * water is greater than that of the neighboring terrain, flood the cells. 
     * Repeat iteratively till the neighboring terrain is higher than the water 
     * height.
     * 
     * 
     * @param height of the water
     * @return boolean[][], where flooded cells are true, otherwise false
     */
    public boolean[][] floodedRegionsIn(double height) {
        
        int numRows = terrain.length;
        int numCols = terrain[0].length;
        boolean[][] flooded = new boolean[numRows][numCols];
    
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                flooded[i][j] = false;
            }
        }
    
        Queue<GridLocation> queue = new LinkedList<>();
    
        for (GridLocation source : sources) {
            int row = source.row;
            int col = source.col;
            if (terrain[row][col] <= height) {
                flooded[row][col] = true;
                queue.offer(source);
            }
        }
    
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, -1, 1 };
    
        while (!queue.isEmpty()) {
            GridLocation current = queue.poll();
            int row = current.row;
            int col = current.col;
    
            for (int i = 0; i < 4; i++) {
                int newRow = row + dx[i];
                int newCol = col + dy[i];
    
                if (newRow >= 0 && newRow < numRows && newCol >= 0 && newCol < numCols) {
                    if (!flooded[newRow][newCol] && terrain[newRow][newCol] <= height) {
                        flooded[newRow][newCol] = true;
                        queue.offer(new GridLocation(newRow, newCol));
                    }
                }
            }
        }
    
        return flooded;
    }

    /**
     * Checks if a given cell is flooded at a certain water height.
     * 
     * @param height of the water
     * @param cell location 
     * @return boolean, true if cell is flooded, otherwise false
     */
    public boolean isFlooded(double height, GridLocation cell) {
        
        int row = cell.row;
        int col = cell.col;
    
        return terrain[row][col] <= height;
    }

    /**
     * Given the water height and a GridLocation find the difference between 
     * the chosen cells height and the water height.
     * 
     * If the return value is negative, the Driver will display "meters below"
     * If the return value is positive, the Driver will display "meters above"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param cell location
     * @return double, representing how high/deep a cell is above/below water
     */
    public double heightAboveWater(double height, GridLocation cell) {
        
        if (terrain == null || terrain.length == 0 || terrain[0].length == 0) {
            return 0.0;
        }
    
        int numRows = terrain.length;
        int numCols = terrain[0].length;
    
        if (cell.row < 0 || cell.row >= numRows || cell.col < 0 || cell.col >= numCols) {
            return 0.0;
        }
    
        return terrain[cell.row][cell.col] - height;
    }

    /**
     * Total land available (not underwater) given a certain water height.
     * 
     * @param height of the water
     * @return int, representing every cell above water
     */
    public int totalVisibleLand(double height) {
        int numRows = terrain.length;
        int numCols = terrain[0].length;
        boolean[][] floodedRegions = floodedRegionsIn(height);
    
        int totalLand = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (!floodedRegions[row][col]) {
                    totalLand++;
                }
            }
        }
    
        return totalLand;
    } 


    /**
     * Given 2 heights, find the difference in land available at each height. 
     * 
     * If the return value is negative, the Driver will display "Will gain"
     * If the return value is positive, the Driver will display "Will lose"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param newHeight the future height of the water
     * @return int, representing the amount of land lost or gained
     */
    public int landLost(double height, double newHeight) {
        
        int currentLandCount = totalVisibleLand(height);
        int futureLandCount = totalVisibleLand(newHeight);
    
        return currentLandCount - futureLandCount;
    }

    /**
     * Count the total number of islands on the flooded terrain.
     * 
     * Parts of the terrain are considered "islands" if they are completely 
     * surround by water in all 8-directions. Should there be a direction (ie. 
     * left corner) where a certain piece of land is connected to another 
     * landmass, this should be considered as one island. A better example 
     * would be if there were two landmasses connected by one cell. Although 
     * seemingly two islands, after further inspection it should be realized 
     * this is one single island. Only if this connection were to be removed 
     * (height of water increased) should these two landmasses be considered 
     * two separate islands.
     * 
     * @param height of the water
     * @return int, representing the total number of islands
     */
    public int numOfIslands(double height) {
        
        int rows = terrain.length;
        int cols = terrain[0].length;
        boolean[][] floodedRegions = floodedRegionsIn(height);
        WeightedQuickUnionUF uf = new WeightedQuickUnionUF(rows, cols);
    
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!floodedRegions[row][col]) {
                    GridLocation current = new GridLocation(row, col);
                    GridLocation[] neighbors = {
                        new GridLocation(row - 1, col - 1),
                        new GridLocation(row - 1, col),
                        new GridLocation(row - 1, col + 1),
                        new GridLocation(row, col - 1)
                    };
    
                    for (GridLocation neighbor : neighbors) {
                        if (neighbor.row >= 0 && neighbor.row < rows && neighbor.col >= 0 && neighbor.col < cols) {
                            if (!floodedRegions[neighbor.row][neighbor.col]) {
                                uf.union(current, neighbor);
                            }
                        }
                    }
                }
            }
        }
    
        Set<GridLocation> uniqueRoots = new HashSet<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!floodedRegions[row][col]) {
                    uniqueRoots.add(uf.find(new GridLocation(row, col)));
                }
            }
        }
    
        return uniqueRoots.size();
    }
}
