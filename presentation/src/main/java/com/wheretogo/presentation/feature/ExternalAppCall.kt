package com.wheretogo.presentation.feature

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.Course
import com.wheretogo.presentation.BuildConfig

fun Context.searchNaverMap(course: Course) {
    val url =
        "nmap://route/car?slat=${course.start.latitude}&slng=${course.start.longitude}&sname=start" +
                "&dlat=${course.goal.latitude}&dlng=${course.goal.longitude}&dname=end" +
                course.waypoints.run {
                    var str = ""
                    this.forEachIndexed { idx, latlng ->
                        str += "&v${idx + 1}lat=${latlng.latitude}&v${idx + 1}lng=${latlng.longitude}&v${idx + 1}name=v${idx + 1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Context.searchTMap(course: Course) {
    val api = TMapTapi(this)
    api.setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)

    api.setOnAuthenticationListener(object : OnAccountsUpdateListener,
        TMapTapi.OnAuthenticationListenerCallback {
        override fun onAccountsUpdated(p0: Array<out Account>?) {

        }

        override fun SKTMapApikeySucceed() {
            val routeMap = HashMap<String, String>()

            course.start.apply {
                routeMap["rStName"] = "출발지"
                routeMap["rStX"] = longitude.toString()
                routeMap["rStY"] = latitude.toString()
            }

            course.goal.apply {
                routeMap["rGoName"] = "목적지"
                routeMap["rGoX"] = longitude.toString()
                routeMap["rGoY"] = latitude.toString()
            }

            course.waypoints.forEachIndexed { idx, latlng ->
                routeMap["rV${idx + 1}Name"] = "경유지 ${idx + 1}"
                routeMap["rV${idx + 1}X"] = latlng.longitude.toString()
                routeMap["rV${idx + 1}Y"] = latlng.latitude.toString()
            }

            if (!api.isTmapApplicationInstalled) {
                api.tMapDownUrl?.let {
                    openPlayStore(it[0])
                    Log.d("tst", "${api.tMapDownUrl}")
                }
            } else {
                api.invokeRoute(routeMap)
            }
        }

        override fun SKTMapApikeyFailed(p0: String?) {
        }
    })


}

fun Context.openPlayStore(url: String) {

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
        setPackage("com.android.vending")
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(webIntent)
    }
}