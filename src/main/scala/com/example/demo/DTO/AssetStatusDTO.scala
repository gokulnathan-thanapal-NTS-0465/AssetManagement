package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus

case class AssetStatusDTO(

                           status: Option[AssetStatus] = None
                         ) 