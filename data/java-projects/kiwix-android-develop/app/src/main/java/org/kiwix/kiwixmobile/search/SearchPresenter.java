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
package org.kiwix.kiwixmobile.search;

import javax.inject.Inject;
import org.kiwix.kiwixmobile.base.BasePresenter;
import org.kiwix.kiwixmobile.data.local.dao.RecentSearchDao;
import org.kiwix.kiwixmobile.database.newdb.dao.NewRecentSearchDao;

/**
 * Created by srv_twry on 14/2/18.
 */

public class SearchPresenter extends BasePresenter<SearchViewCallback> {

  private final NewRecentSearchDao recentSearchDao;

  @Inject SearchPresenter( NewRecentSearchDao recentSearchDao) {
    this.recentSearchDao = recentSearchDao;
  }

  @Override
  public void attachView(SearchViewCallback searchViewCallback) {
    super.attachView(searchViewCallback);
  }

  void getRecentSearches() {
    view.addRecentSearches(recentSearchDao.getRecentSearches());
  }

  void saveSearch(String title) {
    recentSearchDao.saveSearch(title);
  }

  void deleteSearchString(String search) {
    recentSearchDao.deleteSearchString(search);
  }
}