package only.in.ohio.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
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

    // PLACEMENT LOGIC

    public BlockState getPlacementState(ItemPlacementContext ctx)
    {
        var playerFacing = ctx.getPlayerFacing();
        var ew = playerFacing == Direction.EAST || playerFacing == Direction.WEST;
        var shape = ew ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        var facing = Objects.requireNonNull(ctx.getPlayer()).isSneaking() ? playerFacing : Direction.UP;
        return super.getDefaultState().with(SHAPE, shape).with(FACING, facing);
    }

    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor)
    {
        FixSwitchRailFacing(world, pos, state);

        if (neighbor.getDefaultState().emitsRedstonePower() && (new RailPlacementHelper(world, pos, state)).getNeighbors().size() == 3)
        {
            this.updateBlockState(world, pos, state, false);
        }
    }

    private void FixSwitchRailFacing(World world, BlockPos pos, BlockState state)
    {
        if (state.getBlock() instanceof SwitchRailBlock)
        {
            var facing = state.get(FACING);
            if (facing == Direction.UP) return;

            var shape = state.get(SHAPE);
            if (shape == RailShape.EAST_WEST && (facing == Direction.EAST || facing == Direction.WEST)) return;
            if (shape == RailShape.NORTH_SOUTH && (facing == Direction.NORTH || facing == Direction.SOUTH)) return;

            world.setBlockState(pos, state.with(FACING, Direction.UP), 3);
        }
    }

    // INTERACTION LOGIC



    // REAL TRAP SHIT

    public BlockState rotate(BlockState state, BlockRotation rotation)
    {
        return switch (rotation)
        {
            case CLOCKWISE_180 -> switch (state.get(SHAPE))
            {
                case NORTH_SOUTH, EAST_WEST -> state;
                case ASCENDING_EAST -> state.with(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_WEST -> state.with(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_NORTH -> state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_SOUTH -> state.with(SHAPE, RailShape.ASCENDING_NORTH);
                case SOUTH_EAST -> state.with(SHAPE, RailShape.NORTH_WEST);
                case SOUTH_WEST -> state.with(SHAPE, RailShape.NORTH_EAST);
                case NORTH_WEST -> state.with(SHAPE, RailShape.SOUTH_EAST);
                case NORTH_EAST -> state.with(SHAPE, RailShape.SOUTH_WEST);
            };
            case COUNTERCLOCKWISE_90 -> switch (state.get(SHAPE))
            {
                case NORTH_SOUTH -> state.with(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST -> state.with(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST -> state.with(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_WEST -> state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_NORTH -> state.with(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_SOUTH -> state.with(SHAPE, RailShape.ASCENDING_EAST);
                case SOUTH_EAST -> state.with(SHAPE, RailShape.NORTH_EAST);
                case SOUTH_WEST -> state.with(SHAPE, RailShape.SOUTH_EAST);
                case NORTH_WEST -> state.with(SHAPE, RailShape.SOUTH_WEST);
                case NORTH_EAST -> state.with(SHAPE, RailShape.NORTH_WEST);
            };
            case CLOCKWISE_90 -> switch (state.get(SHAPE))
            {
                case NORTH_SOUTH -> state.with(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST -> state.with(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST -> state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_WEST -> state.with(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_NORTH -> state.with(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_SOUTH -> state.with(SHAPE, RailShape.ASCENDING_WEST);
                case SOUTH_EAST -> state.with(SHAPE, RailShape.SOUTH_WEST);
                case SOUTH_WEST -> state.with(SHAPE, RailShape.NORTH_WEST);
                case NORTH_WEST -> state.with(SHAPE, RailShape.NORTH_EAST);
                case NORTH_EAST -> state.with(SHAPE, RailShape.SOUTH_EAST);
            };
            default -> state;
        };
    }

    public BlockState mirror(BlockState state, BlockMirror mirror)
    {
        RailShape railShape = state.get(SHAPE);
        return switch (mirror)
        {
            case LEFT_RIGHT -> switch (railShape)
            {
                case ASCENDING_NORTH -> state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_SOUTH -> state.with(SHAPE, RailShape.ASCENDING_NORTH);
                case SOUTH_EAST -> state.with(SHAPE, RailShape.NORTH_EAST);
                case SOUTH_WEST -> state.with(SHAPE, RailShape.NORTH_WEST);
                case NORTH_WEST -> state.with(SHAPE, RailShape.SOUTH_WEST);
                case NORTH_EAST -> state.with(SHAPE, RailShape.SOUTH_EAST);
                default -> super.mirror(state, mirror);
            };
            case FRONT_BACK -> switch (railShape)
            {
                case ASCENDING_EAST -> state.with(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_WEST -> state.with(SHAPE, RailShape.ASCENDING_EAST);
                default -> super.mirror(state, mirror);
                case SOUTH_EAST -> state.with(SHAPE, RailShape.SOUTH_WEST);
                case SOUTH_WEST -> state.with(SHAPE, RailShape.SOUTH_EAST);
                case NORTH_WEST -> state.with(SHAPE, RailShape.NORTH_EAST);
                case NORTH_EAST -> state.with(SHAPE, RailShape.NORTH_WEST);
            };
            default -> super.mirror(state, mirror);
        };

    }

    public Property<RailShape> getShapeProperty()
    {
        return SHAPE;
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
