package com.zeab.tvturner2.feature.channel.model

case class Channel(
                    id: String,
                    name: String,
                    url: String,
                    mode: String,
                    sources: List[SourceInfo]
                  )
