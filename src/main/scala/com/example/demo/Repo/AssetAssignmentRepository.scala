package com.example.demo.Repo

import com.example.demo.Model.{Asset, AssetAssignment}
import org.springframework.data.jpa.repository.{JpaRepository, Query}


trait AssetAssignmentRepository extends JpaRepository[AssetAssignment, Long] {

  @Query("SELECT aa FROM AssetAssignment aa WHERE aa.asset.id = :assetId AND aa.returnedAt IS NULL")
  def findByAssetIdAndReturnedAtIsNull(assetId:Long):AssetAssignment

  def findAllByUserIdAndReturnedAtIsNull(userId:Long):java.util.List[AssetAssignment]
  
} 