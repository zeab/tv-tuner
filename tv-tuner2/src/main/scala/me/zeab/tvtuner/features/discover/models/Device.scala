package me.zeab.tvtuner.features.discover.models

case class Device(baseURL: String) {

  def toXml: String ={
    s"""<root xmlns="urn:schemas-upnp-org:device-1-0">
      |  <URLBase>$baseURL</URLBase>
      |  <specVersion>
      |    <major>1</major>
      |    <minor>0</minor>
      |  </specVersion>
      |  <device>
      |    <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>
      |    <friendlyName>TvTuner</friendlyName>
      |    <manufacturer>Zeab</manufacturer>
      |    <modelName>HDTC-2US</modelName>
      |    <modelNumber>HDTC-2US</modelNumber>
      |    <serialNumber/>
      |    <UDN>uuid:2020-03-S3LB-BG3LIA:2</UDN>
      |  </device>
      |</root>""".stripMargin
  }

}