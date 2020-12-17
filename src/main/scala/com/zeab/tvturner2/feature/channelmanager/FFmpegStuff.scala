package com.zeab.tvturner2.feature.channelmanager

import scala.sys.process.Process

trait FFmpegStuff {

  def split(file: String, name: String): Int ={
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -c copy -map 0 -segment_time 00:00:30 -f segment C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%03d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 5 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!

    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 5 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -acodec copy -f segment -segment_time 40 -vcodec copy -reset_timestamps 1 -map 0 C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%d.ts").!
    //Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -c copy -map 0 -segment_time 00:00:30 -f segment C:\\Users\\pyros\\Desktop\\filestoplay\\temp\\$name.%03d.ts").!

    Process(s"C:\\Users\\pyros\\Desktop\\ffmpeg-20200821-412d63f-win64-static\\bin\\ffmpeg.exe -i $file -map 0 -codec copy -f segment -segment_time 00:30 C:\\Mine\\tv-tuner\\temp\\$name%03d.ts").!

  }

}
