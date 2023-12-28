package only.in.ohio.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public class SwitchRailBlock extends AbstractRailBlock
{
    public static final EnumProperty<RailShape> SHAPE;
    public static final DirectionProperty FACING;

    protected SwitchRailBlock(AbstractBlock.Settings settings)
    {
        super(true, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SHAPE, RailShape.NORTH_SOUTH).with(FACING, Direction.UP));
    }

    public BlockState getPlacementState(ItemPlacementContext ctx)
    {
        BlockState blockState = super.getDefaultState();
        Direction direction = ctx.getPlayerFacing();
        boolean ew = direction == Direction.EAST || direction == Direction.WEST;
        var shape = ew ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        var facing = Objects.requireNonNull(ctx.getPlayer()).isSneaking() ? direction : Direction.UP;
        return blockState.with(SHAPE, shape).with(FACING, facing);
    }

    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor)
    {
        if (neighbor.getDefaultState().emitsRedstonePower() && (new RailPlacementHelper(world, pos, state)).getNeighbors().size() == 3)
        {
            this.updateBlockState(world, pos, state, false);
        }
    }

    protected BlockState updateBlockState(World world, BlockPos pos, BlockState state, boolean forceUpdate)
    {
        if (world.isClient) return state;
        else
        {
            RailShape railShape = state.get(this.getShapeProperty());
            BlockState blockState = (new RailPlacementHelper(world, pos, state)).updateBlockState(world.isReceivingRedstonePower(pos), forceUpdate, railShape).getBlockState();
            return blockState.with(FACING, Direction.UP);
        }
    }

    /*public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
    {
        var blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof SwitchRailBlock)
        {
            blockState.with(FACING, Direction.UP);
        }
    }*/

    public Property<RailShape> getShapeProperty()
    {
        return SHAPE;
    }

    public BlockState rotate(BlockState state, BlockRotation rotation)
    {
        switch (rotation)
        {
            case CLOCKWISE_180:
                switch ((RailShape) state.get(SHAPE))
                {
                    case ASCENDING_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                }
            case COUNTERCLOCKWISE_90:
                switch ((RailShape) state.get(SHAPE))
                {
                    case NORTH_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_SOUTH);
                    case ASCENDING_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_NORTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                }
            case CLOCKWISE_90:
                switch ((RailShape) state.get(SHAPE))
                {
                    case NORTH_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.EAST_WEST);
                    case EAST_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_SOUTH);
                    case ASCENDING_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    case ASCENDING_NORTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                }
            default:
                return state;
        }
    }

    public BlockState mirror(BlockState state, BlockMirror mirror)
    {
        RailShape railShape = (RailShape) state.get(SHAPE);
        switch (mirror)
        {
            case LEFT_RIGHT:
                switch (railShape)
                {
                    case ASCENDING_NORTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    default:
                        return super.mirror(state, mirror);
                }
            case FRONT_BACK:
                switch (railShape)
                {
                    case ASCENDING_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                }
        }

        return super.mirror(state, mirror);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
    {
        builder.add(SHAPE, FACING);
    }

    static
    {
        SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
        FACING = Properties.FACING;
    }
}
