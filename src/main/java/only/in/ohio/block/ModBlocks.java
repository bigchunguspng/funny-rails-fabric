package only.in.ohio.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import only.in.ohio.Mod;

public class ModBlocks
{
    public static final Block SWITCH_RAIL = registerBlock("switch_rail", new SwitchRailBlock(FabricBlockSettings.of(Material.DECORATION).noCollision().strength(0.7F).sounds(BlockSoundGroup.METAL)), ItemGroup.TRANSPORTATION);

    private static Block registerBlock(String name, Block block, ItemGroup group)
    {
        var id = new Identifier(Mod.MOD_ID, name);
        Registry.register(Registry.ITEM, id, new BlockItem(block, new FabricItemSettings().group(group)));
        return Registry.register(Registry.BLOCK, id, block);
    }

    public static void registerModBlocks()
    {
        System.out.println("Registering blocks for " + Mod.MOD_ID);
    }
}