package com.wheretogo.presentation.feature

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.skt.Tmap.TMapTapi
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.Navigation
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppPermission
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.ExportMap
import com.wheretogo.presentation.feature.naver.getCurrentLocation
import com.wheretogo.presentation.model.CallRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

fun openUri(context: Context, uri: String) {
    val intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
    context.startActivity(intent)
}

fun openWeb(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(context, url.toUri())
}


fun openActivity(context: Context, activity: String) {
    runCatching {
        val intent = Intent(context, Class.forName(activity))
        context.startActivity(intent)
    }
}

suspend fun Context.callMap(
    map: ExportMap,
    navigation: Navigation,
    startMyLocation: Boolean = false
): Result<Unit> {
    return withContext(Dispatchers.Main) {
        runCatching {
            if (startMyLocation) {
                if (!requestPermission(this@callMap, AppPermission.LOCATION))
                    return@withContext Result.failure(AppError.LocationPermissionRequire())
            }
            val myLatlng = if (startMyLocation) getCurrentLocation()?.toLatLng() else null
            when (map) {
                ExportMap.KAKAO -> {
                    if (myLatlng == null)
                        return@withContext Result.failure(AppError.MapNotSupportExcludeLocation())
                    openMap(false, navigation, myLatlng)
                }

                ExportMap.NAVER -> {
                    openMap(true, navigation, myLatlng)
                }

                ExportMap.SKT -> {
                    TMapTapi(this@callMap).openMap(this@callMap, navigation, myLatlng)
                }
            }
        }
    }
}

private fun getKakaoMapUrl(navigation: Navigation, myLatlng: LatLng? = null): String {
    val callRoute = navigation.createCallRoute(myLatlng)
    //카카오는 경유지 호출을 지원하지 않음
    val url = if (navigation.waypoints.size > 2)
        "kakaomap://route?&ep=${callRoute.goal.latitude},${callRoute.goal.longitude}&by=CAR"
    else
        "kakaomap://route?sp=${callRoute.start.latitude},${callRoute.start.longitude}&ep=${callRoute.goal.latitude},${callRoute.goal.longitude}&by=CAR"

    return url
}

private fun getNaverMapUrl(navigation: Navigation, myLatlng: LatLng? = null): String {
    val callRoute = navigation.createCallRoute(myLatlng)
    val startName = if (callRoute.isMyLocaltionStart) "내위치" else "출발지"
    val goalName = navigation.courseName.ifEmpty { "목적지" }
    val url =
        "nmap://route/car?slat=${callRoute.start.latitude}&slng=${callRoute.start.longitude}&sname=$startName" +
                "&dlat=${callRoute.goal.latitude}&dlng=${callRoute.goal.longitude}&dname=$goalName" +
                callRoute.mid.run {
                    var str = ""
                    this.forEachIndexed { idx, latlng ->
                        str += "&v${idx + 1}lat=${latlng.latitude}&v${idx + 1}lng=${latlng.longitude}&v${idx + 1}name=v${idx + 1}"
                    }
                    str
                } +
                "&appname=com.dhkim139.wheretogo"
    return url
}

private var isTmapAuth = false

private suspend fun TMapTapi.init(): Boolean {
    return suspendCancellableCoroutine { con ->
        if (isTmapAuth) {
            con.resume(true)
        } else {
            //setSKTMapAuthentication(BuildConfig.TMAP_APP_KEY)
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

private fun TMapTapi.openMap(context: Context, navigation: Navigation, myLatlng: LatLng? = null) {
    val callRoute = navigation.createCallRoute(myLatlng)

    val routeMap = callRoute.run {
        val routeMap = HashMap<String, String>()
        start.apply {
            routeMap["rStName"] = if (callRoute.isMyLocaltionStart) "내위치" else "출발지"
            routeMap["rStX"] = longitude.toString()
            routeMap["rStY"] = latitude.toString()
        }

        goal.apply {
            routeMap["rGoName"] = navigation.courseName.ifEmpty { "목적지" }
            routeMap["rGoX"] = longitude.toString()
            routeMap["rGoY"] = latitude.toString()
        }

        mid.forEachIndexed { idx, latlng ->
            routeMap["rV${idx + 1}Name"] = "경로 ${idx + 1}"
            routeMap["rV${idx + 1}X"] = latlng.longitude.toString()
            routeMap["rV${idx + 1}Y"] = latlng.latitude.toString()
        }
        routeMap
    }

    if (!invokeRoute(routeMap)) {
        context.openPlayStore("com.skt.tmap.ku")
    }
}

private fun Context.openMap(isNaver: Boolean, navigation: Navigation, myLatlng: LatLng? = null) {
    try {
        val url = if (isNaver) getNaverMapUrl(navigation, myLatlng) else getKakaoMapUrl(
            navigation,
            myLatlng
        )
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (e: Exception) {
        val pkgName = if (isNaver) "com.nhn.android.nmap" else "net.daum.android.map"
        openPlayStore(pkgName)
    }
}

private fun Context.openPlayStore(packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        intent.setPackage("com.android.vending")
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$packageName".toUri()
        )
        startActivity(webIntent)
    }
}

private fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun Navigation.createCallRoute(myLatlng: LatLng?): CallRoute {
    val isMyLocationStart = myLatlng != null
    val callRoute = if (isMyLocationStart) {
        val start = myLatlng!!
        val goal = waypoints.last()
        val mid = waypoints.dropLast(1)
        CallRoute(start, mid, goal, true)
    } else {
        val start = waypoints.first()
        val goal = waypoints.last()
        val mid = waypoints.drop(1).dropLast(1)

        CallRoute(start, mid, goal, false)
    }
    return callRoute
}