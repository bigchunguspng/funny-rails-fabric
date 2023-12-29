package only.in.ohio;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import only.in.ohio.block.ModBlocks;

public class ModClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SWITCH_RAIL, RenderLayer.getCutout());
    }
}