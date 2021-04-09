package org.jebrains.vavilov

import org.jetbrains.vavilov.CallChainToFilterAndMap.SyntaxException
import org.jetbrains.vavilov.CallChainToFilterAndMap.TypeException
import org.jetbrains.vavilov.CallChainToFilterAndMapImpl.callChainToFilterAndMap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CallChainToFilterAndMapTest {

    @Test
    fun givenTests() {
        Assertions.assertEquals(
            "filter{(element>10)&(element<20)}%>%map{element}",
            callChainToFilterAndMap("filter{(element>10)}%>%filter{(element<20)}")
        )

        Assertions.assertEquals(
            "filter{((element+10)>10)}%>%map{((element+10)*(element+10))}",
            callChainToFilterAndMap("map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}")
        )

        Assertions.assertEquals(
            "filter{((element+10)>10)}%>%map{((element+10)*(element+10))}",
            callChainToFilterAndMap("map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}")
        )

        Assertions.assertEquals(
            "filter{((element+10)>10)}%>%map{((element+10)*(element+10))}",
            callChainToFilterAndMap("map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}")
        )

        Assertions.assertEquals(
            "filter{(element>0)&(element<0)}%>%map{(element*element)}",
            callChainToFilterAndMap("filter{(element>0)}%>%filter{(element<0)}%>%map{(element*element)}")
        )
    }

    @Test
    fun testSimpleCases() {
        Assertions.assertEquals(
            "filter{(element>0)}%>%map{element}",
            callChainToFilterAndMap("filter{(element>0)}")
        )

        Assertions.assertEquals(
            "filter{(1=1)}%>%map{(element+1)}",
            callChainToFilterAndMap("map{(element+1)}")
        )

        Assertions.assertEquals(
            "filter{(1=1)}%>%map{element}",
            callChainToFilterAndMap("")
        )


        Assertions.assertEquals(
            "filter{(element>-100)}%>%map{element}",
            callChainToFilterAndMap("filter{(element>-100)}")
        )
    }


    @Test
    fun throwsTests() {
        Assertions.assertThrows(SyntaxException().javaClass) {
            callChainToFilterAndMap("map{(element/10)}")
        }

        Assertions.assertThrows(SyntaxException().javaClass) {
            callChainToFilterAndMap("map{element}%>%")
        }

        Assertions.assertThrows(SyntaxException().javaClass) {
            callChainToFilterAndMap("map{element}+")
        }

        Assertions.assertThrows(TypeException().javaClass) {
            callChainToFilterAndMap("map{(element+(element=element))}")
        }

        Assertions.assertThrows(TypeException().javaClass) {
            callChainToFilterAndMap("map{(element=element)}")
        }

        Assertions.assertThrows(TypeException().javaClass) {
            callChainToFilterAndMap("filter{(element+element)}")
        }
    }


}