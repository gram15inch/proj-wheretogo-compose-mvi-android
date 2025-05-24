package com.wheretogo

import com.wheretogo.domain.feature.courseNameToKeyword
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class SyntaxTest {

    @Test
    fun komoranTest(){
        val actual= courseNameToKeyword("테스트 판교코스1 입니다 \uD83D\uDE06").sorted()
        val expect = listOf("테스트", "판교코스", "판교", "코스", "입니다").sorted()
        assertEquals(expect, actual)
    }
}