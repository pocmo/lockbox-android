/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package mozilla.lockbox.presenter

import android.util.Log
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import mozilla.lockbox.R
import mozilla.lockbox.flux.Dispatcher
import mozilla.lockbox.flux.Presenter
import mozilla.lockbox.model.ItemDetailViewModel
import mozilla.lockbox.model.titleFromHostname
import mozilla.lockbox.store.DataStore
import android.content.ClipData
import android.content.ClipboardManager


interface ItemDetailView {
    var itemId: String?
    fun updateItem(item: ItemDetailViewModel)
    fun copyNotification(strId: Int)

    val clipboardManager: ClipboardManager

    val btnUsernameCopyClicks: Observable<Unit>
    val editUsername: EditText
}

class ItemDetailPresenter(
    private val view: ItemDetailView,
    private val dispatcher: Dispatcher = Dispatcher.shared,
    private val dataStore: DataStore = DataStore.shared

) : Presenter() {

    override fun onViewReady() {

        this.view.btnUsernameCopyClicks
                .subscribe{
                    clipboardCopy("username", view.editUsername.text.toString())
                    view.copyNotification(R.string.toast_username_copied)
                }
                .addTo(compositeDisposable)


    }

    override fun onResume() {
        super.onResume()
        val itemId = view?.itemId ?: return
        dataStore.get(itemId)
                .map {
                    ItemDetailViewModel(it.id, titleFromHostname(it.hostname), it.hostname, it.username, it.password)
                }
                .subscribe(view::updateItem)
                .addTo(compositeDisposable)
    }

    private fun clipboardCopy(label: String, str: String){

        val clip = ClipData.newPlainText(label, str)
        this.view.clipboardManager.primaryClip = clip
    }
}