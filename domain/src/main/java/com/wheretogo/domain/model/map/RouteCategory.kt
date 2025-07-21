package com.wheretogo.domain.model.map

import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.RouteAttrItem
import com.wheretogo.domain.levelCategoryGroup
import com.wheretogo.domain.relationCategoryGroup
import com.wheretogo.domain.typeCategoryGroup


data class RouteCategory(val code:Int, val attr: RouteAttr, val item: RouteAttrItem){
    companion object{
        fun fromCode(strCode:String):RouteCategory?{
           return kotlin.runCatching{
                val code = strCode.toInt()
                (typeCategoryGroup + levelCategoryGroup + relationCategoryGroup).forEach {
                    if (it.code == code)
                        return it
                }
                return null
            }.getOrNull()
        }

        fun fromItem(item:RouteAttrItem):RouteCategory?{
            (typeCategoryGroup + levelCategoryGroup + relationCategoryGroup).forEach {
                if(it.item == item)
                    return it
            }
            return null
        }
    }
}