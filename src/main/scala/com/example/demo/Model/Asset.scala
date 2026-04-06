package com.example.demo.Model

import com.example.demo.Model.Enums.{AssetStatus, Category}
import jakarta.annotation.Nullable
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, GeneratedValue, GenerationType, Id, Table}

import scala.beans.BeanProperty

@Entity
@Table(name = "Assets")
class Asset {
  @Id
  @GeneratedValue(GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = _

  @Column(name = "serial_number", nullable = false)
  @BeanProperty
  var serialNumber: String = _

  @Column(name = "model_name", nullable = false)
  @BeanProperty
  var modelName: String = _

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var status: AssetStatus = _

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var category: Category = _


  @Column(nullable = false)
  @BeanProperty
  var credit:Int = _

}


