package com.dhkim139.wheretogo

import com.wheretogo.presentation.model.Traceable
import com.wheretogo.presentation.model.TraceList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotEquals

class UtilTest {

    @Test
    fun hashCodeTest(){
        data class CA(val a:String, val b:Int)
        val A = CA("a",1)
        val B = CA("a",1)
        assertEquals(A,B)
        assertEquals(A.hashCode(),B.hashCode())
    }

    @Test
    fun traceListTest(){
        data class Person(val name:String, val age:Int): Traceable{
            override fun getFingerPrint(): Int {
                return this.hashCode()
            }
        }
        val p1 = Person("name1",10)
        val p2 = Person("name2",11)
        val p3 = Person("name3",12)
        val p4 = Person("name4",13)
        val list1 = TraceList<Person>({ it.name })
        val list2 = TraceList<Person>({it.name})

        list1.addOrReplace(p1)
        list1.addOrReplace(p2)

        list2.addOrReplace(p2)
        list2.addOrReplace(p1)



        assertEquals(list1.getFingerPrint(false),list2.getFingerPrint(false))
        assertNotEquals(list1.getFingerPrint(true),list2.getFingerPrint(true))

        list1.addOrReplace(p3)
        list2.addOrReplace(p4)

        assertNotEquals(list1.getFingerPrint(false),list2.getFingerPrint(false))

    }
}