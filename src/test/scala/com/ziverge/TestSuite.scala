package com.ziverge
package test

import org.scalatest._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait TestSuite
    extends AnyFunSpec
      with Matchers
      with BeforeAndAfterEach
      with BeforeAndAfter
      with BeforeAndAfterAll
      with ScalaCheckPropertyChecks {

  override implicit val generatorDrivenConfig =
    PropertyCheckConfiguration(minSuccessful = 10)

}
