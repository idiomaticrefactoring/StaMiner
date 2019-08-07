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

package org.kiwix.kiwixmobile.zim_manager

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function6
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.data.DataSource
import org.kiwix.kiwixmobile.data.remote.KiwixService
import org.kiwix.kiwixmobile.database.newdb.dao.NewBookDao
import org.kiwix.kiwixmobile.database.newdb.dao.NewDownloadDao
import org.kiwix.kiwixmobile.database.newdb.dao.NewLanguagesDao
import org.kiwix.kiwixmobile.downloader.Downloader
import org.kiwix.kiwixmobile.downloader.model.DownloadItem
import org.kiwix.kiwixmobile.downloader.model.DownloadModel
import org.kiwix.kiwixmobile.downloader.model.DownloadState.Successful
import org.kiwix.kiwixmobile.downloader.model.DownloadStatus
import org.kiwix.kiwixmobile.downloader.model.UriToFileConverter
import org.kiwix.kiwixmobile.extensions.calculateSearchMatches
import org.kiwix.kiwixmobile.extensions.registerReceiver
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity.Book
import org.kiwix.kiwixmobile.utils.BookUtils
import org.kiwix.kiwixmobile.zim_manager.Fat32Checker.FileSystemState
import org.kiwix.kiwixmobile.zim_manager.Fat32Checker.FileSystemState.CanWrite4GbFile
import org.kiwix.kiwixmobile.zim_manager.Fat32Checker.FileSystemState.CannotWrite4GbFile
import org.kiwix.kiwixmobile.zim_manager.Fat32Checker.FileSystemState.NotEnoughSpaceFor4GbFile
import org.kiwix.kiwixmobile.zim_manager.NetworkState.CONNECTED
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.StorageObserver
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BooksOnDiskListItem
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.adapter.BooksOnDiskListItem.BookOnDisk
import org.kiwix.kiwixmobile.zim_manager.library_view.adapter.LibraryListItem
import org.kiwix.kiwixmobile.zim_manager.library_view.adapter.LibraryListItem.BookItem
import org.kiwix.kiwixmobile.zim_manager.library_view.adapter.LibraryListItem.DividerItem
import java.util.LinkedList
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

class ZimManageViewModel @Inject constructor(
  private val downloadDao: NewDownloadDao,
  private val bookDao: NewBookDao,
  private val languageDao: NewLanguagesDao,
  private val downloader: Downloader,
  private val storageObserver: StorageObserver,
  private val kiwixService: KiwixService,
  private val context: Application,
  private val connectivityBroadcastReceiver: ConnectivityBroadcastReceiver,
  private val bookUtils: BookUtils,
  private val fat32Checker: Fat32Checker,
  private val uriToFileConverter: UriToFileConverter,
  private val defaultLanguageProvider: DefaultLanguageProvider,
  private val dataSource: DataSource
) : ViewModel() {

  val libraryItems: MutableLiveData<List<LibraryListItem>> = MutableLiveData()
  val downloadItems: MutableLiveData<List<DownloadItem>> = MutableLiveData()
  val bookItems: MutableLiveData<List<BooksOnDiskListItem>> = MutableLiveData()
  val deviceListIsRefreshing = MutableLiveData<Boolean>()
  val libraryListIsRefreshing = MutableLiveData<Boolean>()
  val networkStates = MutableLiveData<NetworkState>()
  val languageItems = MutableLiveData<List<Language>>()

  val requestFileSystemCheck = PublishProcessor.create<Unit>()
  val requestDownloadLibrary = BehaviorProcessor.createDefault<Unit>(Unit)
  val requestFiltering = BehaviorProcessor.createDefault<String>("")
  val requestLanguagesDialog = PublishProcessor.create<Unit>()

  private val compositeDisposable = CompositeDisposable()

  init {
    compositeDisposable.addAll(*disposables())
    context.registerReceiver(connectivityBroadcastReceiver)
  }

  @VisibleForTesting
  fun onClearedExposed() {
    onCleared()
  }

  override fun onCleared() {
    compositeDisposable.clear()
    context.unregisterReceiver(connectivityBroadcastReceiver)
    super.onCleared()
  }

  private fun disposables(): Array<Disposable> {
    val downloads = downloadDao.downloads()
    val downloadStatuses = downloadStatuses(downloads)
    val booksFromDao = books()
    val networkLibrary = PublishProcessor.create<LibraryNetworkEntity>()
    val languages = languageDao.languages()
    return arrayOf(
        updateDownloadItems(downloadStatuses),
        removeCompletedDownloadsFromDb(downloadStatuses),
        removeNonExistingDownloadsFromDb(downloadStatuses, downloads),
        updateBookItems(),
        checkFileSystemForBooksOnRequest(booksFromDao),
        updateLibraryItems(booksFromDao, downloads, networkLibrary, languages),
        updateLanguagesInDao(networkLibrary, languages),
        updateNetworkStates(),
        updateLanguageItemsForDialog(languages),
        requestsAndConnectivtyChangesToLibraryRequests(networkLibrary)
    )
  }

  private fun requestsAndConnectivtyChangesToLibraryRequests(library: PublishProcessor<LibraryNetworkEntity>) =
    Flowable.combineLatest(
        requestDownloadLibrary,
        connectivityBroadcastReceiver.networkStates.distinctUntilChanged().filter(
            CONNECTED::equals
        ),
        BiFunction<Unit, NetworkState, Unit> { _, _ -> Unit }
    )
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(
            {
              kiwixService.library
                  .timeout(60, SECONDS)
                  .retry(5)
                  .subscribe(
                      { library.onNext(it) },
                      {
                        it.printStackTrace()
                        library.onNext(LibraryNetworkEntity().apply { book = LinkedList() })
                      }
                  )
            },
            Throwable::printStackTrace
        )

  private fun removeNonExistingDownloadsFromDb(
    downloadStatuses: Flowable<List<DownloadStatus>>,
    downloads: Flowable<List<DownloadModel>>
  ) = downloadStatuses
      .withLatestFrom(
          downloads,
          BiFunction(this::combineToDownloadsWithoutStatuses)
      )
      .buffer(3, SECONDS)
      .map(this::downloadIdsWithNoStatusesOverBufferPeriod)
      .filter { it.isNotEmpty() }
      .subscribe(
          {
            downloadDao.delete(*it.toLongArray())
          },
          Throwable::printStackTrace
      )

  private fun downloadIdsWithNoStatusesOverBufferPeriod(noStatusIds: List<MutableList<Long>>) =
    noStatusIds.flatten()
        .fold(mutableMapOf<Long, Int>(), { acc, id -> acc.increment(id) })
        .filter { (_, count) -> count == noStatusIds.size }
        .map { (id, _) -> id }

  private fun combineToDownloadsWithoutStatuses(
    statuses: List<DownloadStatus>,
    downloads: List<DownloadModel>
  ): MutableList<Long> {
    val downloadIdsWithStatuses = statuses.map { it.downloadId }
    return downloads.fold(
        mutableListOf(),
        { acc, downloadModel ->
          if (!downloadIdsWithStatuses.contains(downloadModel.downloadId)) {
            acc.add(downloadModel.downloadId)
          }
          acc
        }
    )
  }

  private fun updateLanguageItemsForDialog(languages: Flowable<List<Language>>) =
    requestLanguagesDialog
        .withLatestFrom(
            languages,
            BiFunction<Unit, List<Language>, List<Language>> { _, langs -> langs })
        .subscribe(
            languageItems::postValue,
            Throwable::printStackTrace
        )

  private fun updateNetworkStates() =
    connectivityBroadcastReceiver.networkStates.subscribe(
        networkStates::postValue, Throwable::printStackTrace
    )

  private fun updateLibraryItems(
    booksFromDao: Flowable<List<BookOnDisk>>,
    downloads: Flowable<List<DownloadModel>>,
    library: Flowable<LibraryNetworkEntity>,
    languages: Flowable<List<Language>>
  ) = Flowable.combineLatest(
      booksFromDao,
      downloads,
      languages.filter { it.isNotEmpty() },
      library,
      requestFiltering
          .doOnNext { libraryListIsRefreshing.postValue(true) }
          .debounce(500, MILLISECONDS)
          .observeOn(Schedulers.io()),
      fat32Checker.fileSystemStates,
      Function6(this::combineLibrarySources)
  )
      .doOnNext { libraryListIsRefreshing.postValue(false) }
      .subscribeOn(Schedulers.io())
      .subscribe(
          libraryItems::postValue,
          Throwable::printStackTrace
      )

  private fun updateLanguagesInDao(
    library: Flowable<LibraryNetworkEntity>,
    languages: Flowable<List<Language>>
  ) = library
      .subscribeOn(Schedulers.io())
      .map { it.books }
      .withLatestFrom(
          languages,
          BiFunction(this::combineToLanguageList)
      )
      .map { it.sortedBy(Language::language) }
      .filter { it.isNotEmpty() }
      .subscribe(
          languageDao::insert,
          Throwable::printStackTrace
      )

  private fun combineToLanguageList(
    booksFromNetwork: List<Book>,
    allLanguages: List<Language>
  ) = when {
    booksFromNetwork.isEmpty() && allLanguages.isEmpty() -> defaultLanguage()
    booksFromNetwork.isEmpty() && allLanguages.isNotEmpty() -> emptyList()
    booksFromNetwork.isNotEmpty() && allLanguages.isEmpty() ->
      fromLocalesWithNetworkMatchesSetActiveBy(
          networkLanguageCounts(booksFromNetwork), defaultLanguage()
      )
    booksFromNetwork.isNotEmpty() && allLanguages.isNotEmpty() ->
      fromLocalesWithNetworkMatchesSetActiveBy(
          networkLanguageCounts(booksFromNetwork), allLanguages
      )
    else -> throw RuntimeException("Impossible state")
  }

  private fun networkLanguageCounts(booksFromNetwork: List<Book>) =
    booksFromNetwork.mapNotNull { it.language }
        .fold(
            mutableMapOf<String, Int>(),
            { acc, language -> acc.increment(language) }
        )

  private fun <K> MutableMap<K, Int>.increment(key: K) =
    apply { set(key, getOrElse(key, { 0 }) + 1) }

  private fun fromLocalesWithNetworkMatchesSetActiveBy(
    networkLanguageCounts: MutableMap<String, Int>,
    listToActivateBy: List<Language>
  ) = Locale.getISOLanguages()
      .map { Locale(it) }
      .filter { networkLanguageCounts.containsKey(it.isO3Language) }
      .map { locale ->
        Language(
            locale.isO3Language,
            languageIsActive(listToActivateBy, locale),
            networkLanguageCounts.getOrElse(locale.isO3Language, { 0 })
        )
      }

  private fun defaultLanguage() =
    listOf(
        defaultLanguageProvider.provide()
    )

  private fun languageIsActive(
    allLanguages: List<Language>,
    locale: Locale
  ) = allLanguages.firstOrNull { it.languageCode == locale.isO3Language }?.active == true

  private fun combineLibrarySources(
    booksOnFileSystem: List<BookOnDisk>,
    activeDownloads: List<DownloadModel>,
    allLanguages: List<Language>,
    libraryNetworkEntity: LibraryNetworkEntity,
    filter: String,
    fileSystemState: FileSystemState
  ): List<LibraryListItem> {
    val downloadedBooksIds = booksOnFileSystem.map { it.book.id }
    val downloadingBookIds = activeDownloads.map { it.book.id }
    val activeLanguageCodes = allLanguages.filter(Language::active)
        .map { it.languageCode }
    val booksUnfilteredByLanguage =
      applyUserFilter(
          libraryNetworkEntity.books
              .filter {
                when (fileSystemState) {
                  CannotWrite4GbFile -> it.size.toLongOrNull() ?: 0L < Fat32Checker.FOUR_GIGABYTES_IN_KILOBYTES
                  NotEnoughSpaceFor4GbFile,
                  CanWrite4GbFile -> true
                }
              }
              .filterNot { downloadedBooksIds.contains(it.id) }
              .filterNot { downloadingBookIds.contains(it.id) }
              .filterNot { it.url.contains("/stack_exchange/") },// Temp filter see #694, filter)
          filter
      )

    return listOf(
        *createLibrarySection(
            booksUnfilteredByLanguage.filter { activeLanguageCodes.contains(it.language) },
            R.string.your_languages,
            Long.MAX_VALUE
        ),
        *createLibrarySection(
            booksUnfilteredByLanguage.filterNot { activeLanguageCodes.contains(it.language) },
            R.string.other_languages,
            Long.MIN_VALUE
        )
    )
  }

  private fun createLibrarySection(
    books: List<Book>,
    sectionStringId: Int,
    sectionId: Long
  ) =
    if (books.isNotEmpty())
      arrayOf(
          DividerItem(sectionId, context.getString(sectionStringId)),
          *toBookItems(books)
      )
    else emptyArray()

  private fun applyUserFilter(
    booksUnfilteredByLanguage: List<Book>,
    filter: String
  ) = if (filter.isEmpty()) {
    booksUnfilteredByLanguage
  } else {
    booksUnfilteredByLanguage.forEach { it.calculateSearchMatches(filter, bookUtils) }
    booksUnfilteredByLanguage.filter { it.searchMatches > 0 }
  }

  private fun toBookItems(books: List<Book>) =
    books.map { BookItem(it) }.toTypedArray()

  private fun checkFileSystemForBooksOnRequest(booksFromDao: Flowable<List<BookOnDisk>>) =
    requestFileSystemCheck
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .onBackpressureDrop()
        .doOnNext { deviceListIsRefreshing.postValue(true) }
        .switchMap(
            {
              booksFromStorageNotIn(booksFromDao)
            },
            1
        )
        .onBackpressureDrop()
        .doOnNext { deviceListIsRefreshing.postValue(false) }
        .filter { it.isNotEmpty() }
        .map { it.distinctBy { it.book.id } }
        .subscribe(
            bookDao::insert,
            Throwable::printStackTrace
        )

  private fun books() = bookDao.books()
      .subscribeOn(Schedulers.io())
      .map { it.sortedBy { book -> book.book.title } }

  private fun booksFromStorageNotIn(booksFromDao: Flowable<List<BookOnDisk>>) =
    storageObserver.booksOnFileSystem
        .withLatestFrom(
            booksFromDao.map { it.map { bookOnDisk -> bookOnDisk.book.id } },
            BiFunction(this::removeBooksAlreadyInDao)
        )

  private fun removeBooksAlreadyInDao(
    booksFromFileSystem: Collection<BookOnDisk>,
    idsInDao: List<String>
  ) = booksFromFileSystem.filterNot { idsInDao.contains(it.book.id) }

  private fun updateBookItems() =
    dataSource.booksOnDiskAsListItems()
        .subscribe(
            bookItems::postValue,
            Throwable::printStackTrace
        )

  private fun removeCompletedDownloadsFromDb(downloadStatuses: Flowable<List<DownloadStatus>>) =
    downloadStatuses
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .map { it.filter { status -> status.state == Successful } }
        .filter { it.isNotEmpty() }
        .subscribe(
            {
              bookDao.insert(
                  it.map { downloadStatus -> downloadStatus.toBookOnDisk(uriToFileConverter) })
              downloadDao.delete(
                  *it.map { status -> status.downloadId }.toLongArray()
              )
            },
            Throwable::printStackTrace
        )

  private fun updateDownloadItems(downloadStatuses: Flowable<List<DownloadStatus>>) =
    downloadStatuses
        .map { statuses -> statuses.map { DownloadItem(it) } }
        .subscribe(
            downloadItems::postValue,
            Throwable::printStackTrace
        )

  private fun downloadStatuses(downloads: Flowable<List<DownloadModel>>) =
    Flowable.combineLatest(
        downloads,
        Flowable.interval(1, SECONDS),
        BiFunction { downloadModels: List<DownloadModel>, _: Long -> downloadModels }
    )
        .subscribeOn(Schedulers.io())
        .map(downloader::queryStatus)
        .distinctUntilChanged()

}


