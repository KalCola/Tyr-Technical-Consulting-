package com.example

import com.example.tyrconsulting.model.TyrCatalog
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNineRealmsCatalogIntegrity() {
    assertEquals("Should contain exactly 9 realms", 9, TyrCatalog.realms.size)
    assertEquals("Should contain 27 enterprise services", 27, TyrCatalog.services.size)
    assertTrue("All services have non-empty SKUs", TyrCatalog.services.all { it.sku.isNotBlank() })
  }
}
