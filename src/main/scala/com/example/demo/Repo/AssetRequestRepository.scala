package com.example.demo.Repo

import com.example.demo.Model.{Asset, AssetRequest}
import com.example.demo.Model.Enums.{Category, RequestStatus}
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
trait AssetRequestRepository extends JpaRepository[AssetRequest, Long] {

  
  def findAllByStatus(status:RequestStatus): java.util.List[AssetRequest]
  
  def findAllByUserId(userId:Long):java.util.List[AssetRequest]
  
  def findAllByUserIdAndStatus(userId:Long,status:RequestStatus):java.util.List[AssetRequest]
}