package com.sailinghawklabs.gammamaster

import android.app.Dialog
import android.content.Context

import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


open class DialogUtils(private val context: Context) {


    private fun readAssetFile(fileName: String): String? {
        val inputStream: InputStream
        val builder = StringBuilder()
        var htmlString: String? = null
        try {
            inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            htmlString = builder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return htmlString
    }

    fun showAbout() {
        val view = View.inflate(context, R.layout.help_dialog, null)
        val webView = view.findViewById<WebView>(R.id.tv_htmlText)
        var str = readAssetFile(context.getString(R.string.asset_about_html))
        val replaced = str!!.replace("__BVN__", BuildConfig.VERSION_NAME)
        str = replaced.replace("__BVC__", BuildConfig.VERSION_CODE.toString())
        webView.loadDataWithBaseURL("file:///android_asset/", str, "text/html", "UTF-8", null)

        // fill in the title bar
        val titleView = View.inflate(context, R.layout.dialog_title, null)
        val back = titleView.findViewById<ImageView>(R.id.iv_back)
        val title = titleView.findViewById<TextView>(R.id.tv_title)
        title.setText(R.string.about)
        val builder = AlertDialog.Builder(context)
        builder.setCustomTitle(titleView)
        builder.setView(view)
        val dialog: Dialog = builder.create()
        // attach back button
        back.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun showHelp() {
        val view = View.inflate(context, R.layout.help_dialog, null)
        val webView = view.findViewById<WebView>(R.id.tv_htmlText)
        val str = readAssetFile(context.getString(R.string.asset_help_html))
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.loadDataWithBaseURL("file:///android_asset/", str, "text/html", "UTF-8", null)

        // fill in the title bar
        val titleView = View.inflate(context, R.layout.dialog_title, null)
        val back = titleView.findViewById<ImageView>(R.id.iv_back)
        val title = titleView.findViewById<TextView>(R.id.tv_title)
        title.setText(R.string.help)
        val builder = AlertDialog.Builder(context)
        builder.setCustomTitle(titleView)
        builder.setView(view)
        val dialog: Dialog = builder.create()
        // attach back button
        back.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}