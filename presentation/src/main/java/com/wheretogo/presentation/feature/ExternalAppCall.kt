package com.wheretogo.presentation.feature

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.ExportMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun getNaverMapUrl(course: Course): String {
    val start = course.waypoints.first()
    val goal = course.waypoints.last()
    val waypoint = course.waypoints.drop(1).dropLast(1)
    val url =
        "nmap://route/car?slat=${start.latitude}&slng=${start.longitude}&sname=start" +
                "&dlat=${goal.latitude}&dlng=${goal.longitude}&dname=end" +
                waypoint.run {
                    var str = ""
                    this.forEachIndexed { idx, latlng ->
                        str += "&v${idx + 1}lat=${latlng.latitude}&v${idx + 1}lng=${latlng.longitude}&v${idx + 1}name=v${idx + 1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    return url
}

fun Context.callMap(map: ExportMap, course: Course) {
    CoroutineScope(Dispatchers.Main).launch {
        when(map){
            ExportMap.NAVER, ExportMap.KAKAO ->{
                val isNaver = map == ExportMap.NAVER
               openMap(isNaver, course)
            }
            ExportMap.SKT->{
                val tMap = TMapTapi(this@callMap)
                if(tMap.init()) tMap.openMap(this@callMap,course)
            }
        }
    }
}

private fun getKakaoMapUrl(course: Course): String {
    val start = course.waypoints.first()
    val goal = course.waypoints.last()
    val url = if (course.waypoints.size > 2)
        "kakaomap://route?&ep=${goal.latitude},${goal.longitude}&by=CAR"
    else
        "kakaomap://route?sp=${start.latitude},${start.longitude}&ep=${goal.latitude},${goal.longitude}&by=CAR"

    return url
}

private var isTmapAuth = false

private suspend fun TMapTapi.init(): Boolean {
    return suspendCancellableCoroutine { con ->
        if (isTmapAuth) {
            con.resume(true)
        } else {
            setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)
            setOnAuthenticationListener(object : OnAccountsUpdateListener,
                TMapTapi.OnAuthenticationListenerCallback {
                override fun SKTMapApikeySucceed() {
                    isTmapAuth = true
                    con.resume(true)
                }

                override fun onAccountsUpdated(p0: Array<out Account>?) {}
                override fun SKTMapApikeyFailed(p0: String?) {
                    con.resume(false)
                }
            })
        }
    }

}

private fun TMapTapi.openMap(context: Context, course: Course) {

    val start = course.waypoints.first()
    val goal = course.waypoints.last()
    val mid = course.waypoints.drop(1).dropLast(1)
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

    if (!invokeRoute(routeMap).apply {
            Log.d("tst_", "invoke: ${this}")
        }) {
        context.openPlayStore("com.skt.tmap.ku")
    }
}

private fun Context.openMap(isNaver:Boolean, course: Course){
    try {
        val url = if (isNaver) getNaverMapUrl(course) else getKakaoMapUrl(course)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        val pkgName = if (isNaver) "com.nhn.android.nmap" else "net.daum.android.map"
        openPlayStore(pkgName)
    }
}

private fun Context.openPlayStore(packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.setPackage("com.android.vending")
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        startActivity(webIntent)
    }
}

fun openWeb(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}