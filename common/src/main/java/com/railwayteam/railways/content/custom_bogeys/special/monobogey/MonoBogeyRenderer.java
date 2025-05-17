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

package com.railwayteam.railways.content.custom_bogeys.special.monobogey;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;

import static com.railwayteam.railways.registry.CRBlockPartials.*;
import static com.simibubi.create.content.trains.entity.CarriageBogey.UPSIDE_DOWN_KEY;

public class MonoBogeyRenderer implements BogeyRenderer {

    
    @Override
    public void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean inContraption) {
        boolean upsideDown = bogeyData.getBoolean(UPSIDE_DOWN_KEY);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutoutMipped());
        boolean specialUpsideDown = !inContraption && upsideDown; // tile entity renderer needs special handling

        SuperByteBuffer shafts = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.Z));

        for (boolean left : Iterate.trueAndFalse) {
            for (int front : Iterate.positiveAndNegative) {
                shafts.translate(left ? -21 / 16f : 5 / 16f, specialUpsideDown ? 32 / 16f : 0 / 16f, -.5f + front * 8 / 16f)
                        .center()
                        .rotateZ(left ? wheelAngle : -wheelAngle)
                        .uncenter()
                        .renderInto(poseStack, buffer);
            }
        }

        CachedBuffers.partial(MONOBOGEY_FRAME, Blocks.AIR.defaultBlockState())
                .rotateZ(specialUpsideDown ? 180 : 0)
                .translateY(specialUpsideDown ? -3 : 0)
                .renderInto(poseStack, buffer);

        SuperByteBuffer wheels = CachedBuffers.partial(MONOBOGEY_WHEEL,Blocks.AIR.defaultBlockState());
        for (boolean left : Iterate.trueAndFalse) {
            for (int front : Iterate.positiveAndNegative) {
                wheels.translate(left ? -13 / 16f : 13 / 16f, specialUpsideDown ? 32 / 16f : 0 / 16f, front * 16 / 16f)
                        .rotateY(left ? wheelAngle : -wheelAngle)
                        .translate(13 / 16f, 0, 16 / 16f)
                        .renderInto(poseStack, buffer);
            }
        }
    }
}