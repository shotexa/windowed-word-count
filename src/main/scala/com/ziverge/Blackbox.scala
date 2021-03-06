package com.ziverge

import scala.concurrent.Future

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString

object Blackbox {

  private val proc                                   = os.proc("/bin/sh", "-c", "./blackbox")
  private var maybeSubprocess: Option[os.SubProcess] = None

  def start(): Source[ByteString, Future[IOResult]] = {
    val subprocess = proc.spawn()
    maybeSubprocess = Some(subprocess)
    StreamConverters.fromInputStream { () => subprocess.stdout }
  }

  def stop(): Unit = maybeSubprocess.map { subProcess =>
    subProcess.destroy()
    subProcess.destroyForcibly() // in case the subprocess did not stop gracefully
  }

}
