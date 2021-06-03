package com.zeab.tvturner2.service

import java.io.File

import scala.math.BigDecimal.RoundingMode
import scala.sys.process.Process

trait FFmpegStuff {

  def split(file: File): Int = {
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -c copy -map 0 -segment_time 00:00:30 -f segment C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%03d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 5 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!

    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 5 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 40 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -c copy -map 0 -segment_time 00:00:30 -f segment C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%03d.ts").!

    //s"${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -map 0 -segment_time N -reset_timestamps 1 -g XX -sc_threshold 0 -force_key_frames “expr:gte(t,n_forced*N)” -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.mp4"

    //Process(s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -map 0 -segment_time 30 -reset_timestamps 1 -sc_threshold 0 -force_key_frames "expr:gte(t,n_forced*1)" -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.mp4""").!


    //Process(s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -codec copy -map 0 -segment_time 30 -sc_threshold 0 -force_key_frames "expr:gte(t,n_forced*1)" -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.ts""").!


    //Stolen from that site
    //s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -c:v libx264 -crf 22 -map 0 -segment_time 1 -reset_timestamps 1 -g 30 -sc_threshold 0 -force_key_frames "expr:gte(t,n_forced*1)" -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.mp4"""


    //Process(s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -codec copy -map 0 -segment_time 30 -reset_timestamps 1 -sc_threshold 0 -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.ts""").!


//    val frameRate = 29.97
//    val segmentLength = 30
//    val xx = BigDecimal(frameRate * segmentLength).setScale(2, RoundingMode.DOWN)
//    Process(s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -c:v libx265 -c:a copy -map 0 -segment_time $segmentLength -reset_timestamps 1 -sc_threshold 0 -force_key_frames "expr:gte(t,n_forced*$xx)" -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.ts""").!

     Process(s"""${AppConf.ffmpegPath} -i ${file.getAbsoluteFile} -codec copy -map 0 -segment_time 8 -reset_timestamps 1 -sc_threshold 0 -f segment ${new File(AppConf.tempPath).getAbsolutePath}/${file.getName}%03d.ts""").!

  }

}
