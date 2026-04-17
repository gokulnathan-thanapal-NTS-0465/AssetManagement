package com.example.demo.Model

import com.example.demo.Model.Enums.{Category, RequestStatus}
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, ForeignKey, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table}

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@Entity
@Table(name = "AssetRequests")
class AssetRequest {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = uninitialized

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_request_user_id"))
  @BeanProperty
  var user: User = uninitialized

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var category: Category = uninitialized

  @Column(nullable = false)
  @BeanProperty
  var reason: String = uninitialized

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @BeanProperty
  var status: RequestStatus = uninitialized


}



