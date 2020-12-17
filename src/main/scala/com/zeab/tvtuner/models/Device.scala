//package com.zeab.tvtuner.models
//
//case class Device(
////                   friendlyName: String = "mine",
////                   manufacturer: String = "elves",
////                   manufacturerURL: String = "mhmm",
////                   modelNumber: String = "HDTC-2US",
////                   firmwareName: String = "hdhomeruntc_atsc",
////                   tunerCount: String,
////                   firmwareVersion: String,
////                   deviceID: String,
////                   deviceAuth: String,
//                   baseURL: String,
//                   lineupURL: String
//                 ) {
//  def toXml: String ={
//    s"""<root xmlns="urn:schemas-upnp-org:device-1-0">
//      |  <URLBase>$baseURL</URLBase>
//      |  <specVersion>
//      |    <major>1</major>
//      |    <minor>0</minor>
//      |  </specVersion>
//      |  <device>
//      |    <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>
//      |    <friendlyName>dizqueTV2</friendlyName>
//      |    <manufacturer>Silicondust</manufacturer>
//      |    <modelName>HDTC-2US</modelName>
//      |    <modelNumber>HDTC-2US</modelNumber>
//      |    <serialNumber/>
//      |    <UDN>uuid:2020-03-S3LB-BG3LIA:2</UDN>
//      |  </device>
//      |</root>""".stripMargin
//  }
//}