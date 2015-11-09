package com.github.shadowsocks

import java.util.Locale

import android.app.AlertDialog
import android.content.{DialogInterface, Intent}
import android.net.Uri
import android.os.Bundle
import android.preference.{Preference, PreferenceFragment}
import android.webkit.{WebViewClient, WebView}
import com.github.shadowsocks.utils.Key

// TODO: Move related logic here
class ShadowsocksSettings extends PreferenceFragment {
  private lazy val activity = getActivity.asInstanceOf[Shadowsocks]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.pref_all)

    findPreference(Key.isNAT).setOnPreferenceChangeListener((preference: Preference, newValue: Any) =>
      if (ShadowsocksApplication.isRoot) activity.handler.post(() => {
        activity.deattachService()
        activity.attachService()
      }) else false)

    findPreference("recovery").setOnPreferenceClickListener((preference: Preference) => {
      ShadowsocksApplication.track(Shadowsocks.TAG, "reset")
      activity.recovery()
      true
    })

    findPreference("flush_dnscache").setOnPreferenceClickListener((preference: Preference) => {
      ShadowsocksApplication.track(Shadowsocks.TAG, "flush_dnscache")
      activity.flushDnsCache()
      true
    })

    findPreference("about").setOnPreferenceClickListener((preference: Preference) => {
      ShadowsocksApplication.track(Shadowsocks.TAG, "about")
      val web = new WebView(activity)
      web.loadUrl("file:///android_asset/pages/about.html")
      web.setWebViewClient(new WebViewClient() {
        override def shouldOverrideUrlLoading(view: WebView, url: String): Boolean = {
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)))
          true
        }
      })

      new AlertDialog.Builder(activity)
        .setTitle(getString(R.string.about_title).formatLocal(Locale.ENGLISH, ShadowsocksApplication.getVersionName))
        .setCancelable(false)
        .setNegativeButton(getString(android.R.string.ok),
          ((dialog: DialogInterface, id: Int) => dialog.cancel()): DialogInterface.OnClickListener)
        .setView(web)
        .create()
        .show()
      true
    })
  }
}