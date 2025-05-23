package com.wheretogo.domain.feature

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

fun courseNameToKeyword(courseName: String): List<String> {
    val komoran = Komoran(DEFAULT_MODEL.LIGHT)
    val plane = courseName.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z\\s]"), "")
    val words = plane.split(" ").toSet()
    val tokens = komoran.analyze(plane).tokenList.map { it.morph }.toSet()
    val filtered = (tokens + words).filter { it.length > 1 }

    return filtered
}