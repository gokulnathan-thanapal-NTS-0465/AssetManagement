package com.example.demo.Model

import com.example.demo.Model.Enums.ComplaintStatus
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, ForeignKey, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table}

import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@Entity
@Table(name = "Complaints")
class Complaint {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = uninitialized

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_complaint_user_id"))
  @BeanProperty
  var user: User = uninitialized

  @ManyToOne
  @JoinColumn(name = "asset_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_complaint_asset_id"))
  @BeanProperty
  var asset: Asset = uninitialized

  @Column(nullable = false)
  @BeanProperty
  var description: String = uninitialized
  
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @BeanProperty
  var status: ComplaintStatus = uninitialized


}
