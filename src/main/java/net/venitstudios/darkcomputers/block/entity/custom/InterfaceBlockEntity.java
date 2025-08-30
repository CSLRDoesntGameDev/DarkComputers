package net.venitstudios.darkcomputers.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.venitstudios.darkcomputers.block.entity.ModBlockEntities;
import net.venitstudios.darkcomputers.computing.S88.BusDevice;

import java.util.Map;

public class InterfaceBlockEntity extends BlockEntity {
    public InterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.INTERFACE_BE.get(), pos, blockState);
    }
    public BusDevice busDevice;

    private static final Map<Direction, Integer> DIRECTION_MAP = Map.of(
            Direction.NORTH, 0,
            Direction.SOUTH, 1,
            Direction.WEST, 2,
            Direction.EAST, 3,
            Direction.DOWN, 4,
            Direction.UP, 5
    );

    public void initializeWithComputer(ComputerBlockEntity computerBlockEntity) {
        if (computerBlockEntity != null) {
            if (computerBlockEntity.bus != null) {
                if (busDevice == null) {
                    busDevice = new BusDevice(6);
                    computerBlockEntity.bus.addDeviceToBus(busDevice);
                }
            }
        }
    }

    public void setBusStrength(Direction direction, int power) {
        int addr = DIRECTION_MAP.get(direction);
        if (busDevice != null) {
            if ((busDevice.lanes[addr] & 0b01000000) == 0b00000000) {
                busDevice.lanes[addr] = (byte) (power & 0b00001111);
            }
        }
    }

    public int getSignalFromBus(Direction direction) {
        int addr = DIRECTION_MAP.get(direction);
        if (busDevice != null) {
            if ((busDevice.lanes[addr] & 0b01000000) == 0b01000000) {
                return busDevice.lanes[addr] & 0b00001111;
            }
        }
        return 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InterfaceBlockEntity blockEntity) {
    if (level.getBlockEntity(pos) instanceof InterfaceBlockEntity interfaceBlock) {

            if (interfaceBlock.busDevice != null && interfaceBlock.busDevice.hasUpdated) {
                level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
                level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());
                interfaceBlock.busDevice.hasUpdated = false;
            }
        }
    }
}
