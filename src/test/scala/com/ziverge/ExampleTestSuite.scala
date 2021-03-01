package com.ziverge
package test

class ExampleTestSuite extends TestSuite {
  describe("boilerplate") {
    it("example based test") {
      1 shouldBe 1
    }

    it("property based test") {
      forAll { n: Int =>
        n % 1 shouldBe 0
      }
    }
  }
}