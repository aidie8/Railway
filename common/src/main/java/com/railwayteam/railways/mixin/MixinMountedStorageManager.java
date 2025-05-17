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

package com.railwayteam.railways.mixin;

import com.mojang.datafixers.util.Pair;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.railwayteam.railways.util.FluidUtils;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = MountedStorageManager.class, remap = false)
public abstract class MixinMountedStorageManager implements IFuelInventory {
    @Unique private CombinedTankWrapper railways$fluidFuelInventory;
    @Unique private Map<BlockPos, MountedFluidStorage> railways$fluidFuelStorage = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void entityTick(AbstractContraptionEntity entity, CallbackInfo ci) {
        //railways$fluidFuelStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, entity.level.isClientSide));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "addBlock", at = @At("TAIL"))
    private void addBlock(Level level, BlockState state, BlockPos globalPos, BlockPos localPos, @Nullable BlockEntity be, CallbackInfo ci) {
        if (be != null && FluidUtils.canUseAsFuelStorage(be)) {
            MountedFluidStorageType<?> fluidType = MountedFluidStorageType.REGISTRY.get(state.getBlock());
            if (fluidType != null) {
                MountedFluidStorage storage = fluidType.mount(level, state, globalPos, be);
                if (storage != null) {
                    railways$fluidFuelStorage.put(localPos,storage);
                }
            }
        }
    }
    @Inject(method = "read", at = @At("HEAD"))
    private void read(CompoundTag nbt, boolean clientPacket, @Nullable Contraption contraption, CallbackInfo ci) {
        railways$fluidFuelStorage.clear();
        NBTHelper.iterateCompoundList(
                nbt.getList("FluidFuelStorage", Tag.TAG_COMPOUND),
                c -> {
                    BlockPos pos = NbtUtils.readBlockPos(c.getCompound("pos"));
                    CompoundTag data = c.getCompound("storage");
                    MountedFluidStorage.CODEC.decode(NbtOps.INSTANCE, data)
                            .result()
                            .map(Pair::getFirst)
                            .ifPresent(storage -> railways$fluidFuelStorage.put(pos, storage));
                });
    }


    @Inject(method = "write", at = @At("TAIL"))
    private void write(CompoundTag nbt, boolean clientPacket, CallbackInfo ci) {
        ListTag fluidFuelStorageNBT = new ListTag();
        for (BlockPos pos : railways$fluidFuelStorage.keySet()) {
            CompoundTag c = new CompoundTag();
            MountedFluidStorage mountedStorage = railways$fluidFuelStorage.get(pos);

            MountedFluidStorage.CODEC.encodeStart(NbtOps.INSTANCE, mountedStorage).result().ifPresent(encoded -> {
                CompoundTag tag = new CompoundTag();
                tag.put("pos", NbtUtils.writeBlockPos(pos));
                tag.put("Data", encoded);
                fluidFuelStorageNBT.add(c);
            });
        }

        nbt.put("FluidFuelStorage", fluidFuelStorageNBT);
    }

    @Inject(method = "unmount", at = @At("TAIL"))
    public void removeStorageFromWorld(Level level, StructureTemplate.StructureBlockInfo info, BlockPos globalPos, BlockEntity be, CallbackInfo ci) {
        railways$fluidFuelStorage.values()
                .forEach(storage -> storage.type.mount(level,info.state,globalPos,be));
    }

    @Inject(method = "unmount", at = @At("TAIL"))
    private void addStorageToWorld(Level level, StructureTemplate.StructureBlockInfo info, BlockPos globalPos, BlockEntity be, CallbackInfo ci) {
        BlockPos localPos = info.pos();
        BlockState state = info.state();
        if (railways$fluidFuelStorage.containsKey(localPos)) {
            MountedFluidStorage mountedStorage = railways$fluidFuelStorage.get(localPos);
                mountedStorage.unmount(level,state,globalPos,be);
        }
    }

    @Override
    public void railways$setFuelFluids(CombinedTankWrapper combinedTankWrapper) {
        railways$fluidFuelInventory = combinedTankWrapper;
    }

    @Override
    public CombinedTankWrapper railways$getFuelFluids() {
        return railways$fluidFuelInventory;
    }

    @Override
    public void railways$setFluidFuelStorage(Map<BlockPos, MountedFluidStorage> storageMap) {
        railways$fluidFuelStorage = storageMap;
    }

    @Override
    public Map<BlockPos, MountedFluidStorage> railways$getFluidFuelStorage() {
        return railways$fluidFuelStorage;
    }
}
