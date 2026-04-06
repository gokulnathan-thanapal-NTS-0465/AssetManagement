package com.example.demo.Model

import com.example.demo.Model.Enums.UserType
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, GeneratedValue, GenerationType, Id, Table, UniqueConstraint}

import java.time.LocalDate
import java.util.Date
import scala.beans.BeanProperty

@Entity
@Table(name = "Users" )
class User {
  @Id
  @GeneratedValue(GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = _

  @Column(nullable = false)
  @BeanProperty
  var username: String = _

  @Column(name = "password_hash", nullable = false)
  @BeanProperty
  var passwordHash: String = _

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  @BeanProperty
  var userType: UserType = _

  @Column(name = "credit_balance")
  @BeanProperty
  var creditBalance: Int = _

  @Column(name = "employee_id", nullable = false)
  @BeanProperty
  var employeeId: String = _

  @Column(name = "joined_date")
  @BeanProperty
  var joinedDate: LocalDate = _

  @Column(nullable = false)
  @BeanProperty
  var department: String = _
  
  @Column(nullable = false)
  @BeanProperty
  var deactivated:Boolean= _
}
