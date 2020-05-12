package jp.minecraftday.minecraftinstrumentality.utils;

import org.bukkit.World;
import org.bukkit.util.BlockVector;

public class Region {
    private BlockVector max;
    private BlockVector min;
    private BlockVector center;
    private World world;

    public Region(BlockVector max, BlockVector min, BlockVector center, World world){
        this.max = max;
        this.min = min;
        this.center = center;
        this.world = world;

    }

    public BlockVector getMax() {
        return max;
    }

    public BlockVector getMin() {
        return min;
    }

    public BlockVector getCenter() {
        return center;
    }

    public World getWorld() {
        return world;
    }

    /**
     * Get the number of blocks in the region.
     *
     * @return number of blocks
     */
    public int getArea() {
        int x = getWidth();
        int z = getLength();
        return x*z;
    }

    /**
     *
     *
     * @return
     */
    public int getVolume() {
        int x = getWidth();
        int y = getHeight();
        int z = getLength();
        return x*y*z;
    }

    /**
     * Get X-size.
     *
     * @return width
     */
    int getWidth(){
        return max.getBlockX()-min.getBlockX()+1;
    }

    /**
     * Get Y-size.
     *
     * @return height
     */
    int getHeight(){
        return max.getBlockY()-min.getBlockY()+1;
    }

    /**
     * Get Z-size.
     *
     * @return length
     */
    int getLength(){
        return max.getBlockZ()-min.getBlockZ()+1;
    }

}
