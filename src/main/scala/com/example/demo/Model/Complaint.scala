package com.example.demo.Model

import com.example.demo.Model.Enums.ComplaintStatus
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, ForeignKey, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table}

import scala.beans.BeanProperty

@Entity
@Table(name = "Complaints")
class Complaint {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = _

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_complaint_user_id"))
  @BeanProperty
  var user: User = _

  @ManyToOne
  @JoinColumn(name = "asset_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_complaint_asset_id"))
  @BeanProperty
  var asset: Asset = _

  @Column(nullable = false)
  @BeanProperty
  var description: String = _
  
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @BeanProperty
  var status: ComplaintStatus = _


}
