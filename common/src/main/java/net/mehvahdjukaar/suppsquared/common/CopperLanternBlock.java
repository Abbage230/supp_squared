package net.mehvahdjukaar.suppsquared.common;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class CopperLanternBlock extends LightableLanternBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CopperLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Z)
                .setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var b = super.getStateForPlacement(context);
        if (b != null) b = b.setValue(AXIS, context.getHorizontalDirection().getAxis());
        return b;
    }
}
