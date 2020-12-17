package com.zeab.tvturner2.service

import java.io.{BufferedWriter, File, FileWriter}

trait FileHelpers {

  def listAllFiles(files: File): Array[File] = {
    val rootFiles: Array[File] = files.listFiles
    val allFiles: Array[File] =
      if (rootFiles == null) Array.empty
      else rootFiles ++ rootFiles.filter(_.isDirectory).flatMap(listAllFiles)
    allFiles.filterNot(_.isDirectory)
  }

  def writeFile(filename: File, stringToWrite: String, append: Boolean = false): Unit = {
    val file: File = filename
    val bw: BufferedWriter = new BufferedWriter(new FileWriter(file, append))
    bw.write(stringToWrite)
    bw.close()
  }

}
