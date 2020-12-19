package com.zeab.tvturner2.service

import java.io.{BufferedWriter, File, FileWriter}

trait FileHelpers {

  def listAllFiles(path: String): Array[File] = {
    val files: File = new File(path)
    listAllFiles(files)
  }

  def listAllFiles(files: File): Array[File] = {
    val rootFiles: Array[File] = files.listFiles
    val allFiles: Array[File] =
      if (rootFiles == null) Array.empty
      else rootFiles ++ rootFiles.filter(_.isDirectory).flatMap((file: File) => listAllFiles(file))
    allFiles.filterNot(_.isDirectory)
  }

  def writeFile(filename: File, stringToWrite: String, append: Boolean = false): Unit = {
    val file: File = filename
    val bw: BufferedWriter = new BufferedWriter(new FileWriter(file, append))
    bw.write(stringToWrite)
    bw.close()
  }

}
