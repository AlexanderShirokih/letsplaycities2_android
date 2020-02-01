package ru.aleshi.letsplaycities.ui

interface OnRemovableItemClickListener<ItemType> {

    fun onItemClicked(item: ItemType) = Unit

    fun onRemoveItemClicked(item: ItemType, position: Int) = Unit
}