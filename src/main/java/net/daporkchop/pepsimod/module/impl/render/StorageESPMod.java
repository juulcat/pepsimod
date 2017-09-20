/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2017 Team Pepsi
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from Team Pepsi.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: Team Pepsi), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.daporkchop.pepsimod.module.impl.render;

import net.daporkchop.pepsimod.PepsiMod;
import net.daporkchop.pepsimod.module.ModuleCategory;
import net.daporkchop.pepsimod.module.api.Module;
import net.daporkchop.pepsimod.module.api.ModuleOption;
import net.daporkchop.pepsimod.module.api.OptionCompletions;
import net.daporkchop.pepsimod.totally.not.skidded.RenderUtils;
import net.daporkchop.pepsimod.util.PepsiUtils;
import net.daporkchop.pepsimod.util.ReflectionStuff;
import net.daporkchop.pepsimod.util.RenderColor;
import net.minecraft.block.BlockChest;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class StorageESPMod extends Module {
    public static final RenderColor chestColor = new RenderColor(196, 139, 53, 128);
    public static final RenderColor trappedColor = new RenderColor(81, 57, 22, 128);
    public static final RenderColor enderColor = new RenderColor(25, 35, 40, 128);
    public static final RenderColor hopperColor = new RenderColor(45, 45, 45, 128);
    public static final RenderColor furnaceColor = new RenderColor(151, 151, 151, 128);
    public static StorageESPMod INSTANCE;
    public final ArrayList<AxisAlignedBB> basic = new ArrayList<>();
    public final ArrayList<AxisAlignedBB> trapped = new ArrayList<>();
    public final ArrayList<AxisAlignedBB> ender = new ArrayList<>();
    public final ArrayList<AxisAlignedBB> hopper = new ArrayList<>();
    public final ArrayList<AxisAlignedBB> furnace = new ArrayList<>();

    public StorageESPMod(boolean isEnabled, int key, boolean hide) {
        super(isEnabled, "StorageESP", key, hide);
    }

    public static AxisAlignedBB getBoundingBox(World world, BlockPos pos) {
        return world.getBlockState(pos).getBoundingBox(world, pos);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void tick() {
        basic.clear();
        trapped.clear();
        ender.clear();
        hopper.clear();
        furnace.clear();

        for (TileEntity te : PepsiMod.INSTANCE.mc.world.loadedTileEntityList) {
            if ((PepsiMod.INSTANCE.espSettings.basic || PepsiMod.INSTANCE.espSettings.trapped) && te instanceof TileEntityChest) {
                TileEntityChest chestTe = (TileEntityChest) te;

                if (chestTe.adjacentChestXPos != null || chestTe.adjacentChestZPos != null) {
                    continue;
                }

                AxisAlignedBB bb = PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, te.getPos())), te.getPos());

                if (chestTe.adjacentChestXNeg != null) {
                    PepsiUtils.unionBB(bb, PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, chestTe.adjacentChestXNeg.getPos())), chestTe.adjacentChestXNeg.getPos()));
                } else if (chestTe.adjacentChestZNeg != null) {
                    PepsiUtils.unionBB(bb, PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, chestTe.adjacentChestZNeg.getPos())), chestTe.adjacentChestZNeg.getPos()));
                }

                if (chestTe.getChestType() == BlockChest.Type.TRAP) {
                    if (PepsiMod.INSTANCE.espSettings.trapped) {
                        trapped.add(bb);
                    }
                } else {
                    if (PepsiMod.INSTANCE.espSettings.basic) {
                        basic.add(bb);
                    }
                }
            } else if (PepsiMod.INSTANCE.espSettings.ender && te instanceof TileEntityEnderChest) {
                ender.add(PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, te.getPos())), te.getPos()));
            } else if (PepsiMod.INSTANCE.espSettings.furnace && te instanceof TileEntityFurnace) {
                furnace.add(PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, te.getPos())), te.getPos()));
            } else if (PepsiMod.INSTANCE.espSettings.hopper && te instanceof TileEntityHopper) {
                hopper.add(PepsiUtils.offsetBB(PepsiUtils.cloneBB(getBoundingBox(PepsiMod.INSTANCE.mc.world, te.getPos())), te.getPos()));
            }
        }
    }

    @Override
    public void init() {
        INSTANCE = this;
    }

    @Override
    public ModuleOption[] getDefaultOptions() {
        return new ModuleOption[]{
                new ModuleOption<>(PepsiMod.INSTANCE.espSettings.basic, "normal", OptionCompletions.BOOLEAN,
                        (value) -> {
                            PepsiMod.INSTANCE.espSettings.basic = value;
                            return true;
                        },
                        () -> {
                            return PepsiMod.INSTANCE.espSettings.basic;
                        }, "Normal"),
                new ModuleOption<>(PepsiMod.INSTANCE.espSettings.trapped, "trapped", OptionCompletions.BOOLEAN,
                        (value) -> {
                            PepsiMod.INSTANCE.espSettings.trapped = value;
                            return true;
                        },
                        () -> {
                            return PepsiMod.INSTANCE.espSettings.trapped;
                        }, "Trapped"),
                new ModuleOption<>(PepsiMod.INSTANCE.espSettings.ender, "ender", OptionCompletions.BOOLEAN,
                        (value) -> {
                            PepsiMod.INSTANCE.espSettings.ender = value;
                            return true;
                        },
                        () -> {
                            return PepsiMod.INSTANCE.espSettings.ender;
                        }, "Ender"),
                new ModuleOption<>(PepsiMod.INSTANCE.espSettings.hopper, "hopper", OptionCompletions.BOOLEAN,
                        (value) -> {
                            PepsiMod.INSTANCE.espSettings.hopper = value;
                            return true;
                        },
                        () -> {
                            return PepsiMod.INSTANCE.espSettings.hopper;
                        }, "Hopper"),
                new ModuleOption<>(PepsiMod.INSTANCE.espSettings.furnace, "furnace", OptionCompletions.BOOLEAN,
                        (value) -> {
                            PepsiMod.INSTANCE.espSettings.furnace = value;
                            return true;
                        },
                        () -> {
                            return PepsiMod.INSTANCE.espSettings.furnace;
                        }, "Furnace")
        };
    }

    @Override
    public void onRender(float partialTicks) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushMatrix();
        GL11.glTranslated(-ReflectionStuff.getRenderPosX(PepsiMod.INSTANCE.mc.getRenderManager()), -ReflectionStuff.getRenderPosY(PepsiMod.INSTANCE.mc.getRenderManager()), -ReflectionStuff.getRenderPosZ(PepsiMod.INSTANCE.mc.getRenderManager()));

        if (PepsiMod.INSTANCE.espSettings.basic) {
            GL11.glColor4b(chestColor.r, chestColor.g, chestColor.b, chestColor.a);

            basic.forEach((entry) -> {
                RenderUtils.drawOutlinedBox(entry);
            });
        }

        if (PepsiMod.INSTANCE.espSettings.trapped) {
            GL11.glColor4b(trappedColor.r, trappedColor.g, trappedColor.b, trappedColor.a);

            trapped.forEach((entry) -> {
                RenderUtils.drawOutlinedBox(entry);
            });
        }

        if (PepsiMod.INSTANCE.espSettings.ender) {
            GL11.glColor4b(enderColor.r, enderColor.g, enderColor.b, enderColor.a);

            ender.forEach((entry) -> {
                RenderUtils.drawOutlinedBox(entry);
            });
        }

        if (PepsiMod.INSTANCE.espSettings.hopper) {
            GL11.glColor4b(hopperColor.r, hopperColor.g, hopperColor.b, hopperColor.a);

            hopper.forEach((entry) -> {
                RenderUtils.drawOutlinedBox(entry);
            });
        }

        if (PepsiMod.INSTANCE.espSettings.furnace) {
            GL11.glColor4b(furnaceColor.r, furnaceColor.g, furnaceColor.b, furnaceColor.a);

            furnace.forEach((entry) -> {
                RenderUtils.drawOutlinedBox(entry);
            });
        }

        GL11.glPopMatrix();

        // GL resets
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public ModuleCategory getCategory() {
        return ModuleCategory.RENDER;
    }
}