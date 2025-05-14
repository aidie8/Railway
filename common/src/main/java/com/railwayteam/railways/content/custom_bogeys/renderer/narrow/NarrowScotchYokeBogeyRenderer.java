/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_bogeys.renderer.narrow;

import com.jozufozu.flywheel.api.MaterialManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import static com.railwayteam.railways.registry.CRBlockPartials.*;
import static com.railwayteam.railways.registry.CRBlockPartials.NARROW_SCOTCH_WHEEL_PINS;

public class NarrowScotchYokeBogeyRenderer extends BogeyRenderer {
    @Override
    public void initialiseContraptionModelData(MaterialManager materialManager, CarriageBogey carriageBogey) {
        createModelInstance(materialManager, NARROW_SCOTCH_FRAME, NARROW_SCOTCH_WHEELS,
                NARROW_SCOTCH_WHEEL_PINS, NARROW_SCOTCH_PISTONS);
        createModelInstance(materialManager, AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), 2);
        createModelInstance(materialManager, AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), 2);
    }

    @Override
    public BogeySizes.BogeySize getSize() {
        return BogeySizes.LARGE;
    }

    @Override
    public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {
        boolean inInstancedContraption = vb == null;

        BogeyModelData[] primaryShafts = getTransform(AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z), ms, inInstancedContraption, 2);

        for (int i : Iterate.zeroAndOne) {
            primaryShafts[i].translate(-.5, 1 / 16., i * -1)
                    .centre()
                    .rotateZ(wheelAngle)
                    .unCentre()
                    .renderInto(poseStack, buffer);
        }

        BogeyModelData[] secondaryShafts = getTransform(AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X), ms, inInstancedContraption, 2);

        for (int i : Iterate.zeroAndOne) {
            secondaryShafts[i]
                    .translate(-.5f, 6 / 16., (6 / 16.) + i * -(28 / 16.))
                    .centre()
                    .rotateX(wheelAngle)
                    .unCentre()
                    .renderInto(poseStack, buffer);
        }

        getTransform(NARROW_SCOTCH_FRAME, ms, inInstancedContraption)
                .translate(0, 5 / 16f, 0)
                .renderInto(poseStack, buffer);

        getTransform(NARROW_SCOTCH_PISTONS, ms, inInstancedContraption)
                .translate(0, 5 / 16f, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
                .renderInto(poseStack, buffer);

        if (!inInstancedContraption)
            ms.pushPose();

        getTransform(NARROW_SCOTCH_WHEELS, ms, inInstancedContraption)
                .translate(0, 14 / 16., 0)// 14/16
                .rotateX(wheelAngle)
                .translate(0, 0, 0)
                .renderInto(poseStack, buffer);

        getTransform(NARROW_SCOTCH_WHEEL_PINS, ms, inInstancedContraption)
                .translate(0, 14 / 16., 0)
                .rotateX(wheelAngle)
                .translate(0, 1 / 4f, 0)
                .rotateX(-wheelAngle)
                .renderInto(poseStack, buffer);

        if (!inInstancedContraption)
            ms.popPose();
    }
}
