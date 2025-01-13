package com.wheretogo.presentation.feature

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.BuildConfig

fun getNaverMapUrl(course: Course): String {
    val start = course.waypoints.first()
    val goal = course.waypoints.last()
    val url =
        "nmap://route/car?slat=${start.latitude}&slng=${start.longitude}&sname=start" +
                "&dlat=${goal.latitude}&dlng=${goal.longitude}&dname=end" +
                course.waypoints.run {
                    var str = ""
                    this.forEachIndexed { idx, latlng ->
                        str += "&v${idx + 1}lat=${latlng.latitude}&v${idx + 1}lng=${latlng.longitude}&v${idx + 1}name=v${idx + 1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    return url
}

fun Context.callNaverMap(course: Course) {
    val url = getNaverMapUrl(course)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}


fun TMapTapi.CallTMap(course: Course) {
    val start = course.waypoints.first()
    val goal = course.waypoints.last()
    val mid = course.waypoints.drop(1).dropLast(1)
    setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)

    setOnAuthenticationListener(object : OnAccountsUpdateListener,
        TMapTapi.OnAuthenticationListenerCallback {
        override fun onAccountsUpdated(p0: Array<out Account>?) {

        }

        override fun SKTMapApikeySucceed() {
            val routeMap = HashMap<String, String>()

            start.apply {
                routeMap["rStName"] = "출발지"
                routeMap["rStX"] = longitude.toString()
                routeMap["rStY"] = latitude.toString()
            }

            goal.apply {
                routeMap["rGoName"] = "목적지"
                routeMap["rGoX"] = longitude.toString()
                routeMap["rGoY"] = latitude.toString()
            }

            mid.forEachIndexed { idx, latlng ->
                routeMap["rV${idx + 1}Name"] = "경유지 ${idx + 1}"
                routeMap["rV${idx + 1}X"] = latlng.longitude.toString()
                routeMap["rV${idx + 1}Y"] = latlng.latitude.toString()
            }

            if (!isTmapApplicationInstalled) {
                tMapDownUrl?.let {
                    //openPlayStore(it[0])
                    Log.d("tst", "${tMapDownUrl}")
                }
            } else {
                invokeRoute(routeMap)
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

fun openWeb(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}