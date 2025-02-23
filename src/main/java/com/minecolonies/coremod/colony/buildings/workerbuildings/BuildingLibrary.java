package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.entity.ai.util.StudyItem;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BOOKCASES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Creates a new building for the Library.
 */
public class BuildingLibrary extends AbstractBuilding
{
    /**
     * Description of the block used to set this block.
     */
    private static final String LIBRARY_HUT_NAME = "library";

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> bookCases = new ArrayList<>();

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Random obj for random calc.
     */
    private final Random random = new Random();

    /**
     * Study Item list, loaded from config, Typle<int,int> First is the study chance increase,second is the breakchance
     */
    private final List<StudyItem> studyItems;

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingLibrary(final IColony c, final BlockPos l)
    {
        super(c, l);

        studyItems = parseFromConfig();
    }

    /**
     * Parses Study Items from the Config and adds them on the keepX list
     *
     * @return the list of study items
     */
    private List<StudyItem> parseFromConfig()
    {
        final List<StudyItem> studyItemList = new ArrayList<>();

        for (final String entry : MineColonies.getConfig().getServer().configListStudyItems.get())
        {

            try
            {
                final String[] entries = entry.split(";");
                if (entries.length < 3)
                {
                    Log.getLogger().info("Minecolonies: Parsing config for study items for Library failed for entry:" + entry);
                    continue;
                }
                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entries[0]));
                final int skillChance = Integer.parseInt(entries[1]);
                final int breakChance = Integer.parseInt(entries[2]);

                if (item == null || skillChance < 100 || skillChance > 1000 || breakChance > 100 || breakChance < 0)
                {
                    Log.getLogger().info("Minecolonies: Parsing config for study items for Library failed for entry:" + entry);
                    continue;
                }

                studyItemList.add(new StudyItem(item, skillChance, breakChance));
                // Keep a certain part of the items in the Chest
                keepX.put(itemStack -> itemStack.getItem() == item, new Tuple<>(breakChance < 5 ? 5 : breakChance, true));
            }
            catch (NumberFormatException | ClassCastException e)
            {
                Log.getLogger().info("Minecolonies: Parsing config for study items for Library failed for entry:" + entry + " Exception:" + e.getMessage());
            }
        }
        return studyItemList;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return LIBRARY_HUT_NAME;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT furnaceTagList = compound.getList(TAG_BOOKCASES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.size(); ++i)
        {
            bookCases.add(NBTUtil.readBlockPos(furnaceTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT bookcaseTagList = new ListNBT();
        for (@NotNull final BlockPos entry : bookCases)
        {
            @NotNull final CompoundNBT bookCompound = new CompoundNBT();
            bookCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            bookcaseTagList.add(bookCompound);
        }
        compound.put(TAG_BOOKCASES, bookcaseTagList);

        return compound;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        //todo we might in the future want to add our own oredict tag to this.
        if (block == Blocks.BOOKSHELF)
        {
            bookCases.add(pos);
        }
    }

    /**
     * Returns a random bookshelf from the list.
     *
     * @return the position of it.
     */
    public BlockPos getRandomBookShelf()
    {
        if (bookCases.isEmpty())
        {
            return getPosition();
        }
        final BlockPos returnPos = bookCases.get(random.nextInt(bookCases.size()));
        if (colony.getWorld().getBlockState(returnPos).getBlock() == Blocks.BOOKSHELF)
        {
            return returnPos;
        }
        bookCases.remove(returnPos);
        return getPosition();
    }

    public List<StudyItem> getStudyItems()
    {
        return studyItems;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<AbstractBuildingView>(this, LIBRARY_HUT_NAME);
        }
    }
}
