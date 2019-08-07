package org.kiwix.kiwixmobile.zim_manager

import androidx.appcompat.widget.SearchView.OnQueryTextListener

class SimpleTextListener(val onQueryTextChangeAction: (String) -> Unit) : OnQueryTextListener {
  override fun onQueryTextSubmit(s: String): Boolean {
    return false
  }

  override fun onQueryTextChange(s: String): Boolean {
    onQueryTextChangeAction.invoke(s)
    return true
  }
}
