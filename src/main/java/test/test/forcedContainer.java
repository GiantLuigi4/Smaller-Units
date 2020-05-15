package test.test;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

public class forcedContainer extends Container {
    Container container;
    public forcedContainer(Container cnt) {
        Class<? extends Container> cont = cnt.getClass();
        container=forcedContainer.class.cast(cont);
    }

    @Override
    public void addListener(IContainerListener listener) {
        container.addListener(listener);
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return container.getInventory();
    }

    @Override
    public void removeListener(IContainerListener listener) {
        container.removeListener(listener);
    }

    @Override
    public void detectAndSendChanges() {
        container.detectAndSendChanges();
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        return container.enchantItem(playerIn,id);
    }

    @Nullable
    @Override
    public Slot getSlotFromInventory(IInventory inv, int slotIn) {
        return container.getSlotFromInventory(inv,slotIn);
    }

    @Override
    public Slot getSlot(int slotId) {
        return container.getSlot(slotId);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return container.transferStackInSlot(playerIn,index);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        return container.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return container.canMergeSlot(stack, slotIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        container.onContainerClosed(playerIn);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        container.onCraftMatrixChanged(inventoryIn);
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        container.putStackInSlot(slotID, stack);
    }

    @Override
    public void setAll(List<ItemStack> p_190896_1_) {
        container.setAll(p_190896_1_);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        container.updateProgressBar(id, data);
    }

    @Override
    public short getNextTransactionID(InventoryPlayer invPlayer) {
        return container.getNextTransactionID(invPlayer);
    }

    @Override
    public boolean getCanCraft(EntityPlayer player) {
        return container.getCanCraft(player);
    }

    @Override
    public void setCanCraft(EntityPlayer player, boolean canCraft) {
        container.setCanCraft(player, canCraft);
    }

    @Override
    public boolean canDragIntoSlot(Slot slotIn) {
        return container.canDragIntoSlot(slotIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
