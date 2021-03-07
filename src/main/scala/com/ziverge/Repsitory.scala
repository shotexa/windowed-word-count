package com.ziverge
import entities._

object Repository {
  // storing windows in memory
  private var windowHistory: List[Window] = Nil

  def addWindow(window: Window): Unit = {
    windowHistory = window :: windowHistory
  }

  def getAllWindows: List[Window] = windowHistory

  def truncate(): Unit = {
    windowHistory = Nil
  }

}
