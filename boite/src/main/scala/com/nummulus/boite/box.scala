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

package com.nummulus.boite

sealed abstract class Box[+A] {
  self =>
  
  /**
   * Returns {@code true} if the box contains no value (Empty or Failure), 
   * {@code false} otherwise.
   */
  def isEmpty: Boolean
  
  /**
   * Returns {@code true} if the box contains a value, {@code false} otherwise.
   */
  def isDefined: Boolean = !isEmpty
  
  /**
   * Returns the value of the box.
   * 
   * @throws Predef.NoSuchElementException if the option is empty
   */
  def get: A
  
  /**
   * Returns the value of the box if it's full, else the specified default.
   */
  def getOrElse[B >: A](default: => B): B
  
  /**
   * Applies a function to the value of the box if it's full and returns a
   * new box containing the result. Returns empty otherwise.
   * <p>
   * Differs from flatMap in that the given function is not expected to wrap
   * the result in a box.
   * 
   * @see flatMap
   */
  def map[B](f: A => B): Box[B] = Empty
  
  /**
   * Applies a function to the value of the box if it's full and returns a
   * new box containing the result. Returns empty otherwise.
   * <p>
   * Differs from map in that the given function is expected to return a box.
   * 
   * @see map
   */
  def flatMap[B](f: A => Box[B]): Box[B] = Empty
  
  /**
   * Applies a function to the value of the box if it's full, otherwise do 
   * nothing.
   */
  def foreach[U](f: A => U) {}
  
  /**
   * Returns a List of one element if the box is full or an empty list
   * otherwise.
   */
  def toList: List[A] = List()
  
  /**
   * Returns {@code true} if both objects are equal based on the contents of
   * the box. For failures, equality is based on equivalence of failure
   * causes.
   */
  override def equals(other: Any): Boolean = (this, other) match {
    case (Full(x), Full(y)) => x == y
    case (x, y: AnyRef) => x eq y
    case _ => false
  }
  
  override def hashCode: Int = this match {
    case Full(x) => x.##
    case _ => super.hashCode
  }
}

object Box {
  /**
   * Implicitly converts a Box to an Iterable.
   * This is needed, for instance to be able to flatten a List[Box[_]].
   */
  implicit def box2Iterable[A](b: Box[A]): Iterable[A] = b.toList
  
  /**
   * A Box factory which converts a scala.Option to a Box.
   */
  def apply[A](o: Option[A]): Box[A] = o match {
    case Some(value) => Full(value)
    case None => Empty
  }
  
  /**
   * A Box factory which returns a Full(f) if f is not null, Empty if it is,
   * and a Failure if f throws an exception.
   */
  def wrap[A](f: => A): Box[A] =
    try {
      val value = f
      if (value == null) Empty else Full(value)
    }
    catch {
      case e: Exception => Failure(e)
    }
}

final case class Full[+A](value: A) extends Box[A] {
  override def isEmpty = false
  
  override def get: A = value
  
  override def getOrElse[B >: A](default: => B): B = value
  
  override def map[B](f: A => B): Box[B] = Full(f(value))
  
  override def flatMap[B](f: A => Box[B]): Box[B] = f(value)
  
  override def foreach[U](f: A => U) { f(value) }
  
  override def toList: List[A] = List(value)
}

private[boite] sealed abstract class BoiteVide extends Box[Nothing] {
  override def isEmpty = true
  
  override def get: Nothing = throw new NoSuchElementException("Box does not contain a value")
  
  override def getOrElse[B >: Nothing](default: => B): B = default
}

case object Empty extends BoiteVide

sealed case class Failure(exception: Throwable) extends BoiteVide {
  type A = Nothing
  
  override def map[B](f: A => B): Box[B] = this
  
  override def flatMap[B](f: A => Box[B]): Box[B] = this
  
  override final def equals(other: Any): Boolean = (this, other) match {
    case (Failure(x), Failure(a)) => (x) == (a)
    case _ => false
  }
  
  override final def hashCode: Int = exception.##
}

object Failure {
  def apply(message: String) = new Failure(new Exception(message))
}
