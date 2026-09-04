package com.erp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /** 采购报表：按供应商/商品/时间聚合 */
    @Select("<script>SELECT o.supplier_id AS dimId, s.name AS dimName, g.name AS goodsName, " +
            "SUM(i.quantity) AS qty, SUM(i.amount) AS amount, AVG(i.price) AS avgPrice " +
            "FROM purchase_order o JOIN purchase_order_item i ON o.id=i.order_id " +
            "LEFT JOIN crm_supplier s ON o.supplier_id=s.id LEFT JOIN goods g ON i.goods_id=g.id " +
            "WHERE o.deleted=0 AND i.deleted=0 AND o.status!='草稿' " +
            "<if test='startDate!=null and startDate!=\"\"'> AND o.apply_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND o.apply_date&lt;=#{endDate}</if>" +
            "<if test='supplierId!=null'> AND o.supplier_id=#{supplierId}</if>" +
            "<if test='goodsId!=null'> AND i.goods_id=#{goodsId}</if>" +
            " GROUP BY o.supplier_id, s.name, g.name ORDER BY amount DESC</script>")
    List<Map<String, Object>> purchaseReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                             @Param("supplierId") Long supplierId, @Param("goodsId") Long goodsId);

    /** 销售报表：按客户/商品/时间聚合 */
    @Select("<script>SELECT o.customer_id AS dimId, c.name AS dimName, g.name AS goodsName, " +
            "SUM(i.quantity) AS qty, SUM(i.amount) AS amount, SUM(o.discount) AS discount " +
            "FROM sale_order o JOIN sale_order_item i ON o.id=i.order_id " +
            "LEFT JOIN crm_customer c ON o.customer_id=c.id LEFT JOIN goods g ON i.goods_id=g.id " +
            "WHERE o.deleted=0 AND i.deleted=0 AND o.status!='草稿' " +
            "<if test='startDate!=null and startDate!=\"\"'> AND o.order_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND o.order_date&lt;=#{endDate}</if>" +
            "<if test='customerId!=null'> AND o.customer_id=#{customerId}</if>" +
            "<if test='goodsId!=null'> AND i.goods_id=#{goodsId}</if>" +
            " GROUP BY o.customer_id, c.name, g.name ORDER BY amount DESC</script>")
    List<Map<String, Object>> saleReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                         @Param("customerId") Long customerId, @Param("goodsId") Long goodsId);

    /** 低库存 TOP */
    @Select("<script>SELECT s.goods_id AS goodsId, g.code AS goodsCode, g.name AS goodsName, g.low_limit AS lowLimit, " +
            "w.name AS warehouseName, s.quantity, s.quantity-g.low_limit AS gapQty FROM wms_stock s " +
            "LEFT JOIN goods g ON s.goods_id=g.id LEFT JOIN sys_warehouse w ON s.warehouse_id=w.id " +
            "WHERE s.deleted=0 AND g.status='在售' AND s.quantity &lt; g.low_limit " +
            "<if test='warehouseId!=null'> AND s.warehouse_id=#{warehouseId}</if>" +
            " ORDER BY gapQty ASC LIMIT 50</script>")
    List<Map<String, Object>> lowStock(@Param("warehouseId") Long warehouseId);

    /** 财务收支：按账户按天聚合 */
    @Select("<script>SELECT l.biz_date AS bizDate, a.name AS accountName, a.id AS accountId, " +
            "COALESCE(SUM(l.in_amount),0) AS income, COALESCE(SUM(l.out_amount),0) AS expense " +
            "FROM fin_account_log l LEFT JOIN fin_account a ON l.account_id=a.id " +
            "WHERE l.deleted=0 " +
            "<if test='startDate!=null and startDate!=\"\"'> AND l.biz_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND l.biz_date&lt;=#{endDate}</if>" +
            "<if test='accountId!=null'> AND l.account_id=#{accountId}</if>" +
            " GROUP BY l.biz_date, a.name, a.id ORDER BY l.biz_date</script>")
    List<Map<String, Object>> financeReport(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                            @Param("accountId") Long accountId);

    /** 应收账龄 */
    @Select("SELECT " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)<=30 THEN balance ELSE 0 END) AS age30, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>30 AND DATEDIFF(CURDATE(), due_date)<=60 THEN balance ELSE 0 END) AS age60, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>60 AND DATEDIFF(CURDATE(), due_date)<=90 THEN balance ELSE 0 END) AS age90, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>90 THEN balance ELSE 0 END) AS ageOver, " +
            "SUM(balance) AS totalBalance FROM crm_arc_detail WHERE deleted=0 AND status!='已结清'")
    Map<String, Object> arcAging();

    /** 应付账龄 */
    @Select("SELECT " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)<=30 THEN balance ELSE 0 END) AS age30, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>30 AND DATEDIFF(CURDATE(), due_date)<=60 THEN balance ELSE 0 END) AS age60, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>60 AND DATEDIFF(CURDATE(), due_date)<=90 THEN balance ELSE 0 END) AS age90, " +
            "SUM(CASE WHEN DATEDIFF(CURDATE(), due_date)>90 THEN balance ELSE 0 END) AS ageOver, " +
            "SUM(balance) AS totalBalance FROM crm_ap_detail WHERE deleted=0 AND status!='已结清'")
    Map<String, Object> apAging();

    /** 利润汇总：销售毛利 = 销售收入 - 成本(取商品进价) */
    @Select("<script>SELECT g.name AS goodsName, g.code AS goodsCode, " +
            "SUM(i.quantity) AS qty, SUM(i.amount) AS saleAmount, SUM(i.quantity*g.purchase_price) AS costAmount, " +
            "SUM(i.amount - i.quantity*g.purchase_price) AS profit " +
            "FROM sale_order o JOIN sale_order_item i ON o.id=i.order_id " +
            "LEFT JOIN goods g ON i.goods_id=g.id " +
            "WHERE o.deleted=0 AND i.deleted=0 AND o.status!='草稿' " +
            "<if test='startDate!=null and startDate!=\"\"'> AND o.order_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND o.order_date&lt;=#{endDate}</if>" +
            " GROUP BY g.name, g.code ORDER BY profit DESC</script>")
    List<Map<String, Object>> profitReport(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 客户贡献度 */
    @Select("<script>SELECT o.customer_id AS id, c.name AS name, SUM(o.all_amount) AS amount, " +
            "SUM(o.all_amount - (SELECT COALESCE(SUM(i.quantity*g.purchase_price),0) FROM sale_order_item i " +
            "LEFT JOIN goods g ON i.goods_id=g.id WHERE i.order_id=o.id)) AS profit, COUNT(DISTINCT o.id) AS orderCount " +
            "FROM sale_order o LEFT JOIN crm_customer c ON o.customer_id=c.id " +
            "WHERE o.deleted=0 AND o.status!='草稿' " +
            "<if test='startDate!=null and startDate!=\"\"'> AND o.order_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND o.order_date&lt;=#{endDate}</if>" +
            " GROUP BY o.customer_id, c.name ORDER BY amount DESC LIMIT #{topN}</script>")
    List<Map<String, Object>> customerContribution(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                   @Param("topN") int topN);

    /** 供应商贡献度（采购额排行） */
    @Select("<script>SELECT o.supplier_id AS id, s.name AS name, SUM(o.all_amount) AS amount, COUNT(DISTINCT o.id) AS orderCount " +
            "FROM purchase_order o LEFT JOIN crm_supplier s ON o.supplier_id=s.id " +
            "WHERE o.deleted=0 AND o.status!='草稿' " +
            "<if test='startDate!=null and startDate!=\"\"'> AND o.apply_date&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND o.apply_date&lt;=#{endDate}</if>" +
            " GROUP BY o.supplier_id, s.name ORDER BY amount DESC LIMIT #{topN}</script>")
    List<Map<String, Object>> supplierContribution(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                   @Param("topN") int topN);

    /** 库存周转：期间出库量 */
    @Select("<script>SELECT g.id AS goodsId, g.code AS goodsCode, g.name AS goodsName, " +
            "COALESCE(SUM(CASE WHEN l.change_type LIKE '%出库%' THEN ABS(l.change_qty) ELSE 0 END),0) AS outQty, " +
            "COALESCE((SELECT s.quantity FROM wms_stock s WHERE s.goods_id=g.id AND s.deleted=0 LIMIT 1),0) AS curStock " +
            "FROM goods g LEFT JOIN wms_stock_log l ON g.id=l.goods_id AND l.deleted=0 " +
            "<if test='startDate!=null and startDate!=\"\"'> AND l.change_time&gt;=#{startDate}</if>" +
            "<if test='endDate!=null and endDate!=\"\"'> AND l.change_time&lt;=#{endDate}</if>" +
            " WHERE g.deleted=0 AND g.status='在售' GROUP BY g.id, g.code, g.name ORDER BY outQty DESC LIMIT 50</script>")
    List<Map<String, Object>> turnoverReport(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
