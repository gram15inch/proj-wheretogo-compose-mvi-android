package com.wheretogo.domain.feature

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

fun courseNameToKeyword(courseName: String): List<String> {
    return runCatching {
        val komoran = Komoran(DEFAULT_MODEL.LIGHT)
        val words = courseName.split(" ").map {
            it.replace(Regex("[^가-힣ㄱ-ㅎㅏ-ㅣ]"), "")
        }
        val tokens = komoran.analyze(courseName).nouns
       val filtered = (tokens + words).toSet().filter { it .length > 1 }
        filtered
    }.getOrDefault(emptyList())
}