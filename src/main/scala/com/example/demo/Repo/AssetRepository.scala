package com.example.demo.Repo

import com.example.demo.Model.Asset
import com.example.demo.Model.Enums.{AssetStatus, Category}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.stereotype.Repository


@Repository
trait AssetRepository extends JpaRepository[Asset, Long] {
  
  def countByCategory(category: Category): Long
  
  def countByCategoryAndStatus(category: Category, status: AssetStatus): Long

  def findFirstByCategoryAndStatus(category: Category, status: AssetStatus): Asset


  @Query("SELECT a FROM Asset a JOIN AssetAssignment aa ON a.id = aa.asset.id WHERE aa.user.id = :userId AND a.status = :status AND aa.returnedAt is null")
  def findAssetByUserIdAndStatus(status: AssetStatus, userId: Long): java.util.List[Asset]

  @Query("SELECT a FROM Asset a JOIN AssetAssignment aa On a.id=aa.asset.id where aa.user.id=:userId AND aa.returnedAt is null ")
  def findAssetByUserId(userId: Long): java.util.List[Asset]

  @Query("SELECT a FROM Asset a JOIN AssetAssignment aa ON a.id = aa.asset.id WHERE aa.user.id = :userId AND a.status = :status AND a.category=:category AND aa.returnedAt is null")
  def findAssetByUserIdAndStatusAndCategory(status: AssetStatus, userId: Long, category: Category): java.util.List[Asset]

  @Query("SELECT a FROM Asset a JOIN AssetAssignment aa ON a.id = aa.asset.id WHERE aa.user.id = :userId AND a.category=:category AND aa.returnedAt is null")
  def findAssetByUserIdAndCategory(userId: Long, category: Category): java.util.List[Asset]


  @Query("SELECT a FROM Asset a WHERE a.status = :status AND a.category=:category")
  def findAllByStatusAndCategory(status: AssetStatus, category: Category): java.util.List[Asset]

  @Query("SELECT a FROM Asset a WHERE a.status = :status")
  def findAllByStatus(status: AssetStatus): java.util.List[Asset]

  @Query("SELECT a FROM Asset a WHERE a.category=:category")
  def findAllByCategory(category: Category): java.util.List[Asset]
  
  def countByStatus(status: AssetStatus): Long
  
}