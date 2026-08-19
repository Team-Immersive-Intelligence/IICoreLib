package net.iiteam.corelib;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.iiteam.corelib.enums.ISerializableEnum;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 22.06.2019
 */
@SuppressWarnings("unused")
public class Utils
{
	public static <T extends TileEntity & IImmersiveConnectable> Set<Connection> genConnectableBlockstate(T te)
	{
		Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(te.getWorld(), te.getPos());
		if(conns==null)
			return ImmutableSet.of();
		Set<Connection> ret = new HashSet<Connection>()
		{
			@Override
			public boolean equals(Object o)
			{
				if(o==this)
					return true;
				if(!(o instanceof HashSet))
					return false;
				HashSet<Connection> other = (HashSet<Connection>)o;
				if(other.size()!=this.size())
					return false;
				for(Connection c : this)
					if(!other.contains(c))
						return false;
				return true;
			}
		};
		for(Connection c : conns)
		{
			IImmersiveConnectable end = ApiUtils.toIIC(c.end, te.getWorld(), false);
			if(end==null)
				continue;
			c.getSubVertices(te.getWorld());
			ret.add(c);
		}
		return ret;
	}

	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T tank, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, boolean fillBucket)
	{
		return handleBucketTankInteraction(tank, inventory, bucketInputSlot, bucketOutputSlot, fillBucket, fluidStack -> true);
	}

	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T tank, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, boolean fillBucket, Predicate<FluidStack> filter)
	{
		if(inventory.get(bucketInputSlot).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
		{
			IFluidHandlerItem capability = inventory.get(bucketInputSlot).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			FluidStack contents = capability.getTankProperties()[0].getContents();
			int amount_prev = tank.getFluidAmount();
			ItemStack emptyContainer;

			if(fillBucket&&contents==null)
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.fillFluidContainer(tank, inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			else
			{
				if(contents==null||!filter.test(contents))
					return false;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.drainFluidContainer(tank, inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			}

			if(amount_prev!=tank.getFluidAmount())
			{
				if(!inventory.get(bucketOutputSlot).isEmpty()&&OreDictionary.itemMatches(inventory.get(bucketOutputSlot), emptyContainer, true))
					inventory.get(bucketOutputSlot).grow(emptyContainer.getCount());
				else if(inventory.get(bucketOutputSlot).isEmpty())
					inventory.set(bucketOutputSlot, emptyContainer.copy());
				inventory.get(bucketInputSlot).shrink(1);
				if(inventory.get(bucketInputSlot).getCount() <= 0)
					inventory.set(bucketInputSlot, ItemStack.EMPTY);

				return true;
			}
		}
		return false;
	}

	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T[] tanks, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, int tank, boolean fillBucket)
	{
		return handleBucketTankInteraction(tanks, inventory, bucketInputSlot, bucketOutputSlot, tank, fillBucket, fluidStack -> true);
	}

	public static <T extends IFluidTank & IFluidHandler> boolean handleBucketTankInteraction(T[] tanks, NonNullList<ItemStack> inventory, int bucketInputSlot, int bucketOutputSlot, int tank, boolean fillBucket, Predicate<FluidStack> filter)
	{
		if(inventory.get(bucketInputSlot).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
		{
			IFluidHandlerItem capability = inventory.get(bucketInputSlot).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			if(!filter.test(capability.getTankProperties()[0].getContents()))
				return false;

			int amount_prev = tanks[tank].getFluidAmount();
			ItemStack emptyContainer;

			if(fillBucket)
			{
				if(tanks[tank].getTankProperties()[0].getContents()==null)
					return false;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.fillFluidContainer(tanks[tank], inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			}
			else
			{
				if(capability.getTankProperties()[0].getContents()==null)
					return false;
				emptyContainer = blusunrize.immersiveengineering.common.util.Utils.drainFluidContainer(tanks[tank], inventory.get(bucketInputSlot), inventory.get(bucketOutputSlot), null);
			}

			if(amount_prev!=tanks[tank].getFluidAmount())
			{
				if(!inventory.get(bucketOutputSlot).isEmpty()&&OreDictionary.itemMatches(inventory.get(bucketOutputSlot), emptyContainer, true))
					inventory.get(bucketOutputSlot).grow(emptyContainer.getCount());
				else if(inventory.get(bucketOutputSlot).isEmpty())
					inventory.set(bucketOutputSlot, emptyContainer.copy());
				inventory.get(bucketInputSlot).shrink(1);
				if(inventory.get(bucketInputSlot).getCount() <= 0)
					inventory.set(bucketInputSlot, ItemStack.EMPTY);

				return true;
			}
		}
		return false;
	}

	public static boolean outputFluidToTank(FluidTank tank, int amount, BlockPos pos, World world, EnumFacing side)
	{
		if(tank.getFluidAmount() > 0)
		{

			FluidStack out = tank.drain(java.lang.Math.min(tank.getFluidAmount(), amount), false);
			IFluidHandler output = FluidUtil.getFluidHandler(world, pos.offset(side), side.getOpposite());
			if(output!=null)
			{
				int accepted = output.fill(out, true);
				if(accepted > 0)
				{
					int drained = output.fill(blusunrize.immersiveengineering.common.util.Utils.copyFluidStackWithAmount(out, java.lang.Math.min(out.amount, accepted), false), true);
					tank.drain(drained, true);
					return true;
				}
			}
		}
		return false;
	}

	public static String getFluidNameOverlayText(@Nullable FluidStack stack)
	{
		return (stack==null||stack.amount <= 0)?I18n.format(Lib.GUI+"empty"):
				(stack.getLocalizedName()+": "+stack.amount+"mB");
	}

	public static void unlockAdvancement(String modId, EntityPlayer player, String name)
	{
		if(player instanceof EntityPlayerMP)
		{
			//Can't unlock the same advancement twice
			if(hasUnlockedAdvancement(modId, player, name))
				return;

			PlayerAdvancements advancements = ((EntityPlayerMP)player).getAdvancements();
			AdvancementManager manager = ((WorldServer)player.getEntityWorld()).getAdvancementManager();
			Advancement advancement = manager.getAdvancement(new ResourceLocation(modId, name));
			if(advancement!=null)
				advancements.grantCriterion(advancement, "code_trigger");
		}
	}

	public static boolean hasUnlockedAdvancement(String modId, EntityPlayer player, String name)
	{
		if(player instanceof EntityPlayerMP)
		{
			PlayerAdvancements advancements = ((EntityPlayerMP)player).getAdvancements();
			AdvancementManager manager = ((WorldServer)player.getEntityWorld()).getAdvancementManager();
			Advancement advancement = manager.getAdvancement(new ResourceLocation(modId, name));
			if(advancement!=null)
				return advancements.getProgress(advancement).isDone();
		}
		return false;
	}


	public static int cycleInt(boolean forward, int current, int min, int max)
	{
		current += forward?1: -1;
		if(current > max)
			return min;
		else if(current < min)
			return max;
		return current;
	}

	public static int cycleIntAvoid(boolean forward, int current, int min, int max, int avoid)
	{
		int i = cycleInt(forward, current, min, max);
		if(i==avoid)
			return cycleInt(forward, i, min, max);
		else
			return i;
	}

	public static boolean compareBlockstateOredict(IBlockState state, String oreName)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		return blusunrize.immersiveengineering.common.util.Utils.compareToOreName(stack, oreName);
	}


	@Deprecated
	public static String getPowerLevelString(TileEntityMultiblockMetal<?, ?> tile)
	{
		return getPowerLevelString(tile.getEnergyStored(null), tile.getMaxEnergyStored(null));
	}

	public static String getPowerLevelString(FluxStorage storage)
	{
		return getPowerLevelString(storage.getEnergyStored(), storage.getMaxEnergyStored());
	}

	public static String getPowerLevelString(int min, int max)
	{
		return String.format("%s/%s IF", min, max);
	}

	public static void giveOrDropStack(@Nonnull Entity entity, ItemStack stack)
	{
		//attempt to give the item
		if(entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
		{
			IItemHandler capability = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			stack = ItemHandlerHelper.insertItem(capability, stack, false);
		}
		//if can't do that, drop it on the entity's position
		if(!stack.isEmpty())
			blusunrize.immersiveengineering.common.util.Utils.dropStackAtPos(entity.world, entity.getPosition(), stack);
	}

	@Nullable
	public static <T> T requireMaster(@Nullable T object, @Nonnull Function<T, T> master)
	{
		if(object==null)
			return null;
		return master.apply(object);
	}

	/**
	 * <i>Trust me, I'm an Engineer!</i><br>
	 * Returns a value of an annotation for an enum extending {@link ISerializableEnum}<br>
	 * Generally safe to use, but slow. Cache the results.
	 */
	@Nullable
	public static <T extends Annotation> T getEnumAnnotation(Class<T> annotationClass, ISerializableEnum e)
	{
		try
		{
			Field field = e.getClass().getDeclaredField(e.getName().toUpperCase());
			if(field.isAnnotationPresent(annotationClass))
				return field.getAnnotation(annotationClass);
		} catch(NoSuchFieldException ignored) {}
		return null;
	}

	/**
	 * <i>Trust me, I'm an Engineer!</i><br>
	 * Returns a value of an annotation<br>
	 * Generally safe to use, but slow. Cache the results.
	 */
	@Nullable
	public static <T extends Annotation> T getAnnotation(Class<T> annotationClass, Object o)
	{
		if(o.getClass().isAnnotationPresent(annotationClass))
			return o.getClass().getAnnotation(annotationClass);
		return null;
	}



	public static void sendToolbarMessage(EntityPlayer player, String messageFormat, Object... args)
	{
		player.sendStatusMessage(new TextComponentTranslation(messageFormat, args), true);
	}

	public static UUID getBlockPosUUID(BlockPos pos)
	{
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putInt(1234); //Tile Entity
		buffer.putInt(pos.getX());
		buffer.putInt(pos.getY());
		buffer.putInt(pos.getZ());
		return UUID.nameUUIDFromBytes(buffer.array());
	}

	@Nullable
	public static <T> T getTileCapability(World world, BlockPos posIn, Capability<T> capability, EnumFacing facing)
	{
		TileEntity te = world.getTileEntity(posIn);
		if(te==null||!te.hasCapability(capability, facing))
			return null;
		return te.getCapability(capability, facing);
	}

	/**
	 * @param world     the world
	 * @param centerPos the center position of the orb
	 * @param radius    the radius of the orb
	 * @param allowAir  if false non-air blocks will be included in the result
	 * @return blocks in an orb of a given radius
	 */
	public static Set<BlockPos> getBlocksInOrb(World world, BlockPos centerPos, float radius, boolean allowAir)
	{
		ArrayList<BlockPos> set = new ArrayList<>();
		float diameter = radius*radius;

		//Iterate in a cube
		for(float x = -radius; x < radius; x++)
			for(float y = -radius; y < radius; y++)
				for(float z = -radius; z < radius; z++)
				{
					BlockPos pos = centerPos.add(x, y, z);
					//Check if distance is in radius
					if(pos.distanceSq(centerPos) <= diameter)
					{
						if(allowAir||!world.isAirBlock(pos))
							set.add(pos);
					}
				}

		return Sets.newHashSet(set);
	}

	/**
	 * @param world     the world
	 * @param centerPos the center position of the orb
	 * @param radius    the radius of the orb
	 * @return blocks in an orb of a given radius
	 */
	public static Set<BlockPos> getBlocksInOrb(World world, BlockPos centerPos, float radius)
	{
		return getBlocksInOrb(world, centerPos, radius, true);
	}

	public static Set<BlockPos> getBlocksInCube(World world, BlockPos centerPos, float radius)
	{
		ArrayList<BlockPos> set = new ArrayList<>();

		//Iterate in a cube
		for(float x = -radius; x < radius; x++)
			for(float y = -radius; y < radius; y++)
				for(float z = -radius; z < radius; z++)
					set.add(centerPos.add(x, y, z));

		return Sets.newHashSet(set);
	}

}
