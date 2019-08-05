package ru.aleshi.letsplaycities.ui.blacklist

import ru.quandastudio.lpsclient.model.BlackListItem

interface OnItemClickListener {

    fun onRemove(item: BlackListItem, pos: Int)

}