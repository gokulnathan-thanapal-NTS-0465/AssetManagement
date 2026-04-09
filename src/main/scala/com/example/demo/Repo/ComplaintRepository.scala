package com.example.demo.Repo

import com.example.demo.Model.{Asset, Complaint}
import com.example.demo.Model.Enums.ComplaintStatus
import org.springframework.data.jpa.repository.JpaRepository

trait ComplaintRepository extends JpaRepository[Complaint, Long] {


  def findByStatus(status: ComplaintStatus): java.util.List[Complaint]

  def countByStatus(status: ComplaintStatus): Long
  
  def findByUserId(userId:Long):java.util.List[Complaint]
}