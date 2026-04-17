package com.example.demo.Model

import com.example.demo.Model.Enums.{AssetStatus, Category}
import jakarta.annotation.Nullable
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, GeneratedValue, GenerationType, Id, Table}

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@Entity
@Table(name = "Assets")
class Asset {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = uninitialized

  @Column(name = "serial_number", nullable = false)
  @BeanProperty
  var serialNumber: String = uninitialized

  @Column(name = "model_name", nullable = false)
  @BeanProperty
  var modelName: String = uninitialized

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var status: AssetStatus = uninitialized

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var category: Category = uninitialized


  @Column(nullable = false)
  @BeanProperty
  var credit:Int = uninitialized

}


