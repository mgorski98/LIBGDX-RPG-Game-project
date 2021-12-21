package com.rpgproject.inventory

import com.rpgproject.util.observer.TripleArgGameEvent

//maybe add onItemChanged callback?
object Inventory {

    private const val inventorySize = 24

    //todo: change Pair<InventoryItem, Int> to InventorySlotData and make it not nullable
    val items = Array(inventorySize) { InventorySlotData() }
    var currency = 0

    //<editor-fold desc="Inventory callbacks">

    val onArtifactEquipped: Any? = null //this is going to be a callback once implemented
    val onItemAdded = TripleArgGameEvent<InventoryItem?, Int, Int>() //item, position in inventory, current count
    val onItemRemoved = TripleArgGameEvent<InventoryItem?, Int, Int>() //ditto

    //</editor-fold>

    //<editor-fold desc="Item add & remove logic">
    fun addItem(item: InventoryItem, count: Int): Int {
        var currentCount = count
        for (index in items.indices) {
            if (currentCount <= 0) {
                break
            }
            val current = items[index]
            if (current.item != null && current.item == item && current.amount < item.maxStack) {
                val stackCount = current.amount
                var newSize = count + stackCount
                if (newSize > item.maxStack) {
                    currentCount -= (item.maxStack - stackCount)
                    newSize = item.maxStack
                }
                current.amount = newSize
                onItemAdded.invoke(item, index, newSize)
            } else if (current.item == null) {
                var addedCount: Int
                if (item.maxStack < currentCount) {
                    currentCount -= item.maxStack
                    addedCount = item.maxStack
                } else{
                    addedCount = currentCount
                    currentCount = 0
                }
                current.item = item
                current.amount += addedCount
                onItemAdded.invoke(item, index, addedCount)
            } else continue
        }

        return currentCount
    }

    fun removeItem(item: InventoryItem, count: Int): Boolean {
        val slotsWithItem = ArrayList<Pair<Int, Int>>() //list of pairs index: count
        var currentCount = count
        for (index in items.indices) {
            if (currentCount <= 0) break
            val current = items[index]
            if (current.item != null && current.item == item) {
                var subtractedFromSlot = 0
                if (currentCount >= current.amount) {
                    currentCount -= current.amount
                    subtractedFromSlot = current.amount
                } else {
                    subtractedFromSlot = current.amount - currentCount
                    currentCount = 0
                }
                slotsWithItem += Pair(index, subtractedFromSlot)
            }
        }

        if (currentCount > 0) {
            return false
        }

        slotsWithItem.forEach { (slotIndex, subtractedCount) -> kotlin.run {
            val currentItem = items[slotIndex]
            val currentItemCount = currentItem.amount - subtractedCount
            currentItem.item = if (currentItemCount <= 0) null else currentItem.item
            onItemRemoved.invoke(currentItem.item, slotIndex, currentItemCount)
        } }

        return true
    }

    fun itemAt(position: Int) = items[position]
    //</editor-fold>

    fun reset() {
        for (i in items.indices) {
            val item = items[i]
            if (item != null) {

            }
        }

        onItemAdded.clearListeners()
        onItemRemoved.clearListeners()
    }
}
