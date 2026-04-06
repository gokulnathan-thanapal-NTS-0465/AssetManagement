package com.example.demo.Model

import com.example.demo.Model.Enums.Category

object Credit {

  var creditRequirements: Map[Category, Int] = Map(
    Category.LAPTOP -> 75,
    Category.MOBILE -> 50,
    Category.DESKTOP -> 75,
    Category.KEYBOARD -> 50,
    Category.MOUSE -> 15
  )
}
