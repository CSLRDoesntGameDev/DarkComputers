package net.venitstudios.darkcomputers.computing.S88;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.venitstudios.darkcomputers.block.entity.custom.InterfaceBlockEntity;

public class BusDevice {
    public BusS88 bus;
    public int laneCount = 0;
    public Object root;
    public boolean hasUpdated = false;
    public byte[] lanes;
    public BusDevice(int laneCount) {
        this.laneCount = laneCount;
        this.lanes = new byte[laneCount];
    }

}
