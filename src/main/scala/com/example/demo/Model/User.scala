package com.example.demo.Model

import com.example.demo.Model.Enums.UserType
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.{Column, Entity, EnumType, Enumerated, GeneratedValue, GenerationType, Id, Table, UniqueConstraint}

import java.time.LocalDate
import java.util.Date
import scala.beans.BeanProperty
import scala.compiletime.uninitialized

@Entity
@Table(name = "Users" )
class User {
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(nullable = false)
  @BeanProperty
  var id: Long = uninitialized

  @Column(nullable = false)
  @BeanProperty
  var username: String = uninitialized

  @Column(name = "password_hash", nullable = false)
  @BeanProperty
  var passwordHash: String = uninitialized

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  @BeanProperty
  var userType: UserType = uninitialized

  @Column(name = "credit_balance")
  @BeanProperty
  var creditBalance: Int = uninitialized

  @Column(name = "employee_id", nullable = false)
  @BeanProperty
  var employeeId: String = uninitialized

  @Column(name = "joined_date")
  @BeanProperty
  var joinedDate: LocalDate = uninitialized

  @Column(nullable = false)
  @BeanProperty
  var department: String = uninitialized
  
  @Column(nullable = false)
  @BeanProperty
  var deactivated:Boolean= uninitialized
}
