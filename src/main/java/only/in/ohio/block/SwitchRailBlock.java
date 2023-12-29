package only.in.ohio.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SwitchRailBlock extends AbstractRailBlock
{
    public static final EnumProperty<RailShape> SHAPE;
    public static final DirectionProperty FACING;

    protected SwitchRailBlock(AbstractBlock.Settings settings)
    {
        super(true, settings); // note: "allowCurves" parameter in fact DISABLES curves
        this.setDefaultState(this.stateManager.getDefaultState().with(SHAPE, RailShape.NORTH_SOUTH).with(FACING, Direction.UP));
    }

    // PLACEMENT LOGIC

    public BlockState getPlacementState(ItemPlacementContext ctx)
    {
        var playerFacing = ctx.getPlayerFacing();
        var shape = isEastOrWest(playerFacing) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
        var facing = Objects.requireNonNull(ctx.getPlayer()).isSneaking() ? playerFacing : Direction.UP;
        return super.getDefaultState().with(SHAPE, shape).with(FACING, facing);
    }

    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor)
    {
        FixSwitchRailFacing(world, pos, state);

        // idk what the code below does and when it's called
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
            if (shape == RailShape.EAST_WEST && isEastOrWest(facing)) return;
            if (shape == RailShape.NORTH_SOUTH && isNorthOrSouth(facing)) return;

            world.setBlockState(pos, state.with(FACING, Direction.UP), 3);
        }
    }

    // INTERACTION LOGIC

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
    {
        if (world.isClient) return;

        if (entity.getType() != EntityType.MINECART) return;

        var passengers = entity.getPassengerList();
        if (passengers.isEmpty()) return;

        var passenger = passengers.get(0);

        var rail = world.getBlockState(pos);
        if (rail.getBlock() instanceof SwitchRailBlock)
        {
            var direction = getCartDirection(entity.getVelocity());

            // one-way switch can only shape rails in front of it
            if (isOneWaySwitch(rail) && rail.get(FACING) == direction.getOpposite()) return;

            var ahead = pos.offset(direction);
            var next = world.getBlockState(ahead);
            if (next.getBlock() != Blocks.RAIL) return;

            // ascending tracks can't be switched
            if (RailIsAscending(next)) return;

            var shape = getNewRailShape(world, ahead, direction, passenger.getHeadYaw());

            world.setBlockState(ahead, next.with(Properties.RAIL_SHAPE, shape), 3);
        }
    }

    private static boolean isOneWaySwitch(BlockState rail)
    {
        return rail.get(FACING) != Direction.UP;
    }

    private static Direction getCartDirection(Vec3d speed)
    {
        if /**/ (speed.getX() != 0) return speed.getX() > 0 ? Direction.EAST : Direction.WEST;
        else if (speed.getZ() != 0) return speed.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;

        else return Direction.UP;
    }

    private static boolean RailIsAscending(BlockState rail)
    {
        var s = rail.get(Properties.RAIL_SHAPE);
        return s == RailShape.ASCENDING_EAST || s == RailShape.ASCENDING_WEST || s == RailShape.ASCENDING_NORTH || s == RailShape.ASCENDING_SOUTH;
    }

    private static RailShape getNewRailShape(World world, BlockPos pos, Direction cartDirection, float passengerYaw)
    {
        var routes = new ArrayList<Direction>();
        routes.add(Direction.NORTH);
        routes.add(Direction.EAST);
        routes.add(Direction.SOUTH);
        routes.add(Direction.WEST);

        // can't turn backwards
        routes.remove(cartDirection.getOpposite());

        for (int i = 3; i > 0; )
        {
            var direction = routes.get(--i);

            var ahead = pos.offset(direction);
            var next = world.getBlockState(ahead);
            var down = world.getBlockState(ahead.down());

            // can't be derailed + can't turn to one-way switch pointing to them
            if (!(blockIsAnyRail(next) || blockIsAnyRail(down)) || isOppositeOneWaySwitch(next, direction))
            {
                routes.remove(i);
            }
        }

        // go straight if there's nowhere to turn
        if (routes.isEmpty()) return getRailShape(cartDirection, cartDirection);

        var angles = routes.stream().map(x -> angleDifference(passengerYaw, getDirectionYaw(x))).toList();

        var smallest = Collections.min(angles);
        var route = routes.get(angles.indexOf(smallest));

        return getRailShape(cartDirection, route);
    }

    private static boolean blockIsAnyRail(BlockState blockState)
    {
        return BlockTags.RAILS.contains(blockState.getBlock());
    }

    private static boolean isOppositeOneWaySwitch(BlockState rail, Direction turn)
    {
        var isSwitchRail = rail.getBlock() instanceof SwitchRailBlock;
        return isSwitchRail && isOneWaySwitch(rail) && rail.get(FACING) == turn.getOpposite();
    }

    private static float getDirectionYaw(Direction direction)
    {
        return switch (direction)
        {
            case SOUTH -> 0;
            case WEST -> 90;
            case NORTH -> 180;
            default -> 270;
        };
    }

    private static float angleDifference(float yaw1, float yaw2)
    {
        var abs = Math.abs(PositiveAngle(yaw1) - PositiveAngle(yaw2));
        return abs > 180 ? 360 - abs : abs;
    }

    private static float PositiveAngle(float x)
    {
        return (x + 3600) % 360;
    }

    private static RailShape getRailShape(Direction straight, Direction turn)
    {
        var ns = isNorthOrSouth(straight);

        if (straight == turn) return ns ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;

        var a = straight.getOpposite().name();
        var b = turn.name();

        return RailShape.valueOf(MessageFormat.format(ns ? "{0}_{1}" : "{1}_{0}", a, b));
    }

    private static boolean isNorthOrSouth(Direction direction)
    {
        return direction == Direction.NORTH || direction == Direction.SOUTH;
    }

    private static boolean isEastOrWest(Direction direction)
    {
        return direction == Direction.EAST || direction == Direction.WEST;
    }

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
