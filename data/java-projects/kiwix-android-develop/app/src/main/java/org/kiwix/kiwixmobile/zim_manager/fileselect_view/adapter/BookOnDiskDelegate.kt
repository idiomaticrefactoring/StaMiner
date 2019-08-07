/*
 * Kiwix Android
 * Copyright (C) 2018  Kiwix <android.kiwix.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter

import android.view.ViewGroup
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.extensions.inflate
import org.kiwix.kiwixmobile.utils.SharedPreferenceUtil
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BookOnDiskViewHolder.BookViewHolder
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BookOnDiskViewHolder.LanguageItemViewHolder
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BooksOnDiskListItem.BookOnDisk
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BooksOnDiskListItem.LanguageItem
import org.kiwix.kiwixmobile.zim_manager.library_view.adapter.base.AbsDelegateAdapter

sealed class BookOnDiskDelegate<I : BooksOnDiskListItem, VH : BookOnDiskViewHolder<I>> :
    AbsDelegateAdapter<I, BooksOnDiskListItem, VH> {

  class BookDelegate(
    val sharedPreferenceUtil: SharedPreferenceUtil,
    val clickAction: (BookOnDisk) -> Unit,
    val longClickAction: ((BookOnDisk) -> Unit)? = null
  ) : BookOnDiskDelegate<BookOnDisk, BookViewHolder>() {

    override val itemClass = BookOnDisk::class.java

    override fun createViewHolder(parent: ViewGroup) =
      BookViewHolder(
          parent.inflate(R.layout.item_book, false),
          sharedPreferenceUtil,
          clickAction,
          longClickAction
      )
  }

  object LanguageDelegate : BookOnDiskDelegate<LanguageItem, LanguageItemViewHolder>() {

    override val itemClass = LanguageItem::class.java

    override fun createViewHolder(parent: ViewGroup) =
      LanguageItemViewHolder(parent.inflate(R.layout.header_language, false))
  }
}
