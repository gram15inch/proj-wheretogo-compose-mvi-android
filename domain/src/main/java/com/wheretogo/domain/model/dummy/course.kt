package com.wheretogo.domain.model.dummy

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course

val guideCourse = Course.dummy.copy(
    id = "CS01JNQ7DZCV6FD3KKNQP3R2VM77",
    title = "남한산성 와인딩",
    center = LatLng(
        latitude= 37.46134080586441,
        longitude = 127.15620568795299
    )
)

val guideCheckPoint = CheckPoint(
    courseId = "CS01JNQ7DZCV6FD3KKNQP3R2VM77",
    checkPointId = "CP01K9A646506PHT91XW71N85BC7",
    latLng = LatLng(
        latitude = 37.4688158,
        longitude = 127.167737
    ),
    imageId = "IM01K9JH9V13S53BBDKY8XG4KY54"
)