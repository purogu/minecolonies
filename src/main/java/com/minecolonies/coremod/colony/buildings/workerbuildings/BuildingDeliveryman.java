package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;

/**
 * Class of the warehouse building.
 */
public class BuildingDeliveryman extends AbstractBuilding implements IBuildingDeliveryman
{

    private static final String DELIVERYMAN = "deliveryman";

    /**
     * Instantiates a new warehouse building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDeliveryman(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DELIVERYMAN;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        final ICitizenData citizenData = getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
        if (citizenData != null)
        {
            final JobDeliveryman job = (JobDeliveryman) citizenData.getJob();
            final IRequest<? extends IRequestable> currentTask = job.getCurrentTask();
            if (currentTask == null)
            {
                return super.canEat(stack);
            }
            final IRequestable request = currentTask.getRequest();
            if (request instanceof Delivery && ((Delivery) request).getStack().sameItem(stack))
            {
                return false;
            }
        }
        return super.canEat(stack);
    }

    /**
     * BuildingDeliveryman View.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiate the deliveryman view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, DELIVERYMAN);
        }
    }
}
