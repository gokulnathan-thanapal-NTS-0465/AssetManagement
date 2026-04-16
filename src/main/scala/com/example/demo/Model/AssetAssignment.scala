
package com.example.demo.Model

import jakarta.persistence.{Column, Entity, EnumType, Enumerated, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, ForeignKey}

import java.time.LocalDateTime
import scala.beans.BeanProperty

@Entity
@Table(name = "AssetAssignments")
class AssetAssignment {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = _

  @BeanProperty
  @ManyToOne
  @JoinColumn(name = "asset_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_assignment_asset_id"))
  var asset: Asset = _

  @BeanProperty
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, foreignKey = new ForeignKey(name = "fk_assignment_user_id"))
  var user: User = _

  @BeanProperty
  @Column(name = "assigned_at", nullable = false)
  var assignedAt: LocalDateTime = _

  @BeanProperty
  @Column(name = "returned_at")
  var returnedAt: LocalDateTime = _

}
