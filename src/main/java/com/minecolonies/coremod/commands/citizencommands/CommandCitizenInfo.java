package com.minecolonies.coremod.commands.citizencommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

import static com.minecolonies.coremod.commands.CommandArgumentNames.CITIZENID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Displays information about a chosen citizen in a chosen colony.
 */
public class CommandCitizenInfo implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {

        final Entity sender = context.getSource().getEntity();
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? World.OVERWORLD : context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notfound"), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notloaded"), true);
            return 0;
        }

        context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.desc", citizenData.getId(), citizenData.getName()), true);

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

        final BlockPos citizenPosition = entityCitizen.blockPosition();
        context.getSource().sendSuccess(LanguageHandler.buildChatComponent(
          "com.minecolonies.command.citizeninfo.pos",
          citizenPosition.getX(),
          citizenPosition.getY(),
          citizenPosition.getZ()), true);
        final BlockPos homePosition = entityCitizen.getRestrictCenter();
        context.getSource()
          .sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.homepos", homePosition.getX(), homePosition.getY(), homePosition.getZ()), true);

        if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.workposnull"), true);
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getCitizenColonyHandler().getWorkBuilding().getPosition();
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent(
              "com.minecolonies.command.citizeninfo.workpos",
              workingPosition.getX(),
              workingPosition.getY(),
              workingPosition.getZ()), true);
        }

        context.getSource()
          .sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.health", entityCitizen.getHealth(), entityCitizen.getMaxHealth()), true);

        Object[] skills = new Object[] {
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Athletics).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Dexterity).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Strength).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Agility).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Stamina).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Mana).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Adaptability).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Focus).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Creativity).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Knowledge).getA(),
          citizenData.getCitizenSkillHandler().getSkills().get(Skill.Intelligence).getA()
        };
        context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.skills",
          skills), true);

        if (entityCitizen.getCitizenJobHandler().getColonyJob() == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.jobnull"), true);
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.noactivity"), true);
        }
        else if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() != null && entityCitizen.getCitizenColonyHandler().getWorkBuilding().hasModule(WorkerBuildingModule.class))
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent(
              "com.minecolonies.command.citizeninfo.job",
              entityCitizen.getCitizenColonyHandler().getWorkBuilding().getFirstModuleOccurance(WorkerBuildingModule.class).getJobEntry().getTranslationKey()), true);
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.activity",
              entityCitizen.getDesiredActivity(),
              entityCitizen.getCitizenJobHandler().getColonyJob().getNameTagDescription(),
              entityCitizen.goalSelector.getRunningGoals().findFirst().get().getGoal().toString()),
              true);
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "info";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute)));
    }
}
