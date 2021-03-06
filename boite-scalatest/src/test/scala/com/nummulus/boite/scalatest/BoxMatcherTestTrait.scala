/*
 * Copyright 2012 Nummulus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nummulus.boite.scalatest

import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.ShouldMatchers

import com.nummulus.boite._

trait BoxMatcherTestTrait extends ShouldMatchers {
  val Message = "this is the exception message"
  val Exception = new IllegalStateException(Message)
  
  val FullWithFoo = Full("foo")
  val FailureWithException = Failure(Exception)
  
  def itShouldFailSaying(substrings: String*)(block: => Unit) {
    val msg = intercept[TestFailedException](block).getMessage
    substrings foreach { msg should include (_) }
  }
}