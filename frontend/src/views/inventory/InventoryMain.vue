<template>
  <div>
    <div class="page-title">进销存管理</div>
    <div class="erp-card">
      <el-tabs v-model="tab">
        <!-- 商品档案 -->
        <el-tab-pane label="商品档案" name="goods">
          <div class="toolbar">
            <div>
              <el-input v-model="goodsQuery.keyword" placeholder="名称/编码" clearable style="width: 180px" />
              <el-select v-model="goodsQuery.categoryId" clearable placeholder="分类" style="width: 150px; margin-left: 8px">
                <el-option v-for="c in goodsCats" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadGoods">查询</el-button>
            </div>
            <el-button type="primary" @click="openGoods()">新增商品</el-button>
          </div>
          <el-table :data="goodsPage.list" border>
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="name" label="商品名称" min-width="150" />
            <el-table-column prop="spec" label="规格" width="100" />
            <el-table-column prop="categoryName" label="分类" width="110" />
            <el-table-column prop="unitName" label="单位" width="80" />
            <el-table-column prop="purchasePrice" label="进价" width="90" align="right" />
            <el-table-column prop="salePrice" label="售价" width="90" align="right" />
            <el-table-column prop="stockQty" label="库存" width="90" align="right" />
            <el-table-column prop="status" label="状态" width="80"><template #default="{ row }"><el-tag size="small" :type="row.status === '在售' ? 'success' : 'info'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openGoods(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delGoods(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="goodsPage.total" :page-size="10" v-model:current-page="goodsQuery.page" @current-change="loadGoods" /></div>
        </el-tab-pane>

        <!-- 商品分类 -->
        <el-tab-pane label="商品分类" name="gcat">
          <div class="toolbar"><el-button type="primary" @click="openGcat()">新增分类</el-button></div>
          <el-table :data="gcatTree" row-key="id" :tree-props="{ children: 'children' }" border>
            <el-table-column prop="name" label="分类名称" min-width="220" />
            <el-table-column prop="sort" label="排序" width="100" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openGcat(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delGcat(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 计量单位 -->
        <el-tab-pane label="计量单位" name="unit">
          <div class="toolbar"><el-button type="primary" @click="openUnit()">新增单位</el-button></div>
          <el-table :data="units" border>
            <el-table-column prop="name" label="单位名称" min-width="160" />
            <el-table-column prop="code" label="编码" width="140" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openUnit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delUnit(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 采购需求 -->
        <el-tab-pane label="采购需求" name="demand">
          <div class="toolbar">
            <div>
              <el-select v-model="demandQuery.status" clearable placeholder="状态" style="width: 140px">
                <el-option label="待处理" value="待处理" /><el-option label="已生成采购" value="已生成采购" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadDemands">查询</el-button>
              <el-button type="warning" style="margin-left: 8px" :disabled="!selectedDemands.length" @click="toOrderDlg = true">生成采购单</el-button>
            </div>
            <el-button type="primary" @click="openDemand()">新增需求</el-button>
          </div>
          <el-table :data="demandPage.list" border @selection-change="rows => selectedDemands = rows">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="demandNo" label="需求单号" width="150" />
            <el-table-column prop="goodsName" label="商品" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="90" align="right" />
            <el-table-column prop="applicant" label="申请人" width="100" />
            <el-table-column prop="needDate" label="需求日期" width="120" />
            <el-table-column prop="status" label="状态" width="110"><template #default="{ row }"><el-tag size="small" :type="row.status === '待处理' ? 'warning' : 'success'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button size="small" type="danger" @click="delDemand(row)">删除</el-button></template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="demandPage.total" :page-size="10" v-model:current-page="demandQuery.page" @current-change="loadDemands" /></div>
        </el-tab-pane>

        <!-- 采购单 -->
        <el-tab-pane label="采购单" name="purchase">
          <div class="toolbar">
            <div>
              <el-input v-model="poQuery.keyword" placeholder="单号/供应商" clearable style="width: 180px" />
              <el-select v-model="poQuery.status" clearable placeholder="状态" style="width: 140px; margin-left: 8px">
                <el-option v-for="s in ['草稿','待审批','已通过','部分到货','已到货','结算','结单']" :key="s" :label="s" :value="s" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadPOs">查询</el-button>
            </div>
            <el-button type="primary" @click="openPO()">新建采购单</el-button>
          </div>
          <el-table :data="poPage.list" border>
            <el-table-column prop="orderNo" label="采购单号" width="160" />
            <el-table-column prop="supplierName" label="供应商" min-width="150" />
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="applyDate" label="下单日期" width="110" />
            <el-table-column prop="allAmount" label="金额" width="110" align="right"><template #default="{ row }"><span class="money">{{ row.allAmount }}</span></template></el-table-column>
            <el-table-column prop="auditStatus" label="审核" width="90"><template #default="{ row }"><el-tag size="small" :type="row.auditStatus === '已审核' ? 'success' : row.auditStatus === '待审核' ? 'warning' : 'info'">{{ row.auditStatus }}</el-tag></template></el-table-column>
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '结单' ? 'info' : 'primary'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="openPODetail(row)">详情</el-button>
                <el-button size="small" @click="openPO(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delPO(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="poPage.total" :page-size="10" v-model:current-page="poQuery.page" @current-change="loadPOs" /></div>
        </el-tab-pane>

        <!-- 采购票据 -->
        <el-tab-pane label="采购票据" name="bill">
          <div class="toolbar"><el-button type="primary" @click="openBill()">登记票据</el-button></div>
          <el-table :data="bills" border>
            <el-table-column prop="orderNo" label="关联采购单" width="160" />
            <el-table-column prop="billType" label="票据类型" width="120" />
            <el-table-column prop="billNo" label="票据号码" min-width="160" />
            <el-table-column prop="amount" label="金额" width="120" align="right" />
            <el-table-column prop="registerTime" label="登记时间" width="170" />
          </el-table>
        </el-tab-pane>

        <!-- 销售单 -->
        <el-tab-pane label="销售单" name="sale">
          <div class="toolbar">
            <div>
              <el-input v-model="soQuery.keyword" placeholder="单号/客户" clearable style="width: 180px" />
              <el-select v-model="soQuery.status" clearable placeholder="状态" style="width: 140px; margin-left: 8px">
                <el-option v-for="s in ['草稿','待审批','已通过','部分出库','已出库','已结算','结单']" :key="s" :label="s" :value="s" />
              </el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadSOs">查询</el-button>
            </div>
            <el-button type="primary" @click="openSO()">新建销售单</el-button>
          </div>
          <el-table :data="soPage.list" border>
            <el-table-column prop="orderNo" label="销售单号" width="160" />
            <el-table-column prop="customerName" label="客户" min-width="150" />
            <el-table-column prop="orderDate" label="订单日期" width="110" />
            <el-table-column prop="allAmount" label="金额" width="110" align="right"><template #default="{ row }"><span class="money">{{ row.allAmount }}</span></template></el-table-column>
            <el-table-column prop="receivedAmount" label="已收款" width="100" align="right" />
            <el-table-column prop="auditStatus" label="审核" width="90"><template #default="{ row }"><el-tag size="small" :type="row.auditStatus === '已审核' ? 'success' : row.auditStatus === '待审核' ? 'warning' : 'info'">{{ row.auditStatus }}</el-tag></template></el-table-column>
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag size="small" :type="row.status === '结单' ? 'info' : 'primary'">{{ row.status }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button size="small" type="primary" plain @click="openSODetail(row)">详情</el-button>
                <el-button size="small" @click="openSO(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="delSO(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="soPage.total" :page-size="10" v-model:current-page="soQuery.page" @current-change="loadSOs" /></div>
        </el-tab-pane>

        <!-- 销售退货 -->
        <el-tab-pane label="销售退货" name="return">
          <div class="toolbar">
            <div>
              <el-select v-model="returnStatus" clearable placeholder="状态" style="width: 140px"><el-option label="已退" value="已退" /></el-select>
              <el-button type="primary" style="margin-left: 8px" @click="loadReturns">查询</el-button>
            </div>
            <el-button type="primary" @click="openReturn()">登记退货</el-button>
          </div>
          <el-table :data="returnPage.list" border>
            <el-table-column prop="returnNo" label="退货单号" width="150" />
            <el-table-column prop="srcType" label="来源" width="100" />
            <el-table-column prop="reason" label="退货原因" min-width="200" />
            <el-table-column prop="amount" label="金额" width="110" align="right" />
            <el-table-column prop="status" label="状态" width="90" />
            <el-table-column prop="returnDate" label="退货日期" width="120" />
          </el-table>
          <div class="pagination-bar"><el-pagination layout="total, prev, pager, next" :total="returnPage.total" :page-size="10" v-model:current-page="returnQuery.page" @current-change="loadReturns" /></div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ============ 商品弹窗 ============ -->
    <el-dialog v-model="goodsDlg" :title="goodsForm.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form :model="goodsForm" label-width="90px">
        <el-form-item label="编码"><el-input v-model="goodsForm.code" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="goodsForm.name" /></el-form-item>
        <el-form-item label="分类">
          <el-tree-select v-model="goodsForm.categoryId" :data="gcatTree" :props="{ label: 'name', value: 'id' }" check-strictly style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="goodsForm.unitId" style="width: 100%"><el-option v-for="u in units" :key="u.id" :label="u.name" :value="u.id" /></el-select>
        </el-form-item>
        <el-form-item label="规格"><el-input v-model="goodsForm.spec" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="goodsForm.brand" /></el-form-item>
        <el-form-item label="条码"><el-input v-model="goodsForm.barcode" /></el-form-item>
        <el-form-item label="进价"><el-input-number v-model="goodsForm.purchasePrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="售价"><el-input-number v-model="goodsForm.salePrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="库存下限"><el-input-number v-model="goodsForm.lowLimit" :min="0" :precision="3" style="width: 100%" /></el-form-item>
        <el-form-item label="库存上限"><el-input-number v-model="goodsForm.highLimit" :min="0" :precision="3" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="goodsForm.status" style="width: 100%"><el-option label="在售" value="在售" /><el-option label="停售" value="停售" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="goodsDlg = false">取消</el-button><el-button type="primary" @click="saveGoods">保存</el-button></template>
    </el-dialog>

    <!-- 分类/单位弹窗 -->
    <el-dialog v-model="gcatDlg" :title="gcatForm.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="gcatForm" label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="gcatForm.name" /></el-form-item>
        <el-form-item label="上级分类"><el-tree-select v-model="gcatForm.parentId" :data="gcatTree" :props="{ label: 'name', value: 'id' }" check-strictly clearable placeholder="无" style="width: 100%" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="gcatForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="gcatDlg = false">取消</el-button><el-button type="primary" @click="saveGcat">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="unitDlg" :title="unitForm.id ? '编辑单位' : '新增单位'" width="420px">
      <el-form :model="unitForm" label-width="80px">
        <el-form-item label="单位名称"><el-input v-model="unitForm.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="unitForm.code" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="unitDlg = false">取消</el-button><el-button type="primary" @click="saveUnit">保存</el-button></template>
    </el-dialog>

    <!-- 需求弹窗 -->
    <el-dialog v-model="demandDlg" title="新增采购需求" width="440px">
      <el-form :model="demandForm" label-width="90px">
        <el-form-item label="商品">
          <el-select v-model="demandForm.goodsId" filterable style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="demandForm.quantity" :min="0.001" :precision="3" style="width: 100%" /></el-form-item>
        <el-form-item label="需求日期"><el-date-picker v-model="demandForm.needDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="demandForm.note" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="demandDlg = false">取消</el-button><el-button type="primary" @click="saveDemand">保存</el-button></template>
    </el-dialog>

    <!-- 需求转采购单 -->
    <el-dialog v-model="toOrderDlg" title="由需求生成采购单" width="480px">
      <el-form label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="toOrderForm.supplierId" filterable style="width: 100%"><el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" /></el-select>
        </el-form-item>
        <el-form-item label="入库仓库">
          <el-select v-model="toOrderForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="下单日期"><el-date-picker v-model="toOrderForm.applyDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="toOrderDlg = false">取消</el-button><el-button type="primary" @click="doToOrder">生成</el-button></template>
    </el-dialog>

    <!-- 采购单编辑弹窗 -->
    <el-dialog v-model="poDlg" :title="poForm.id ? '编辑采购单' : '新建采购单'" width="860px" top="4vh">
      <el-form :model="poForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="供应商"><el-select v-model="poForm.supplierId" filterable style="width: 100%"><el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="下单日期"><el-date-picker v-model="poForm.applyDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="入库仓库"><el-select v-model="poForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="税率"><el-input-number v-model="poForm.taxRate" :min="0" :precision="2" /><span style="margin-left: 8px; color: #909399">%</span></el-form-item>
        <el-form-item label="备注"><el-input v-model="poForm.remark" /></el-form-item>
        <el-form-item label="明细">
          <el-table :data="poForm.items" border max-height="260" style="width: 100%">
            <el-table-column label="商品" width="220">
              <template #default="{ row }">
                <el-select v-model="row.goodsId" filterable size="small" style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="130">
              <template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.001" :precision="3" size="small" style="width: 100%" /></template>
            </el-table-column>
            <el-table-column label="单价" width="130">
              <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" style="width: 100%" /></template>
            </el-table-column>
            <el-table-column label="金额" width="110">
              <template #default="{ row }"><span class="money">{{ (row.quantity * row.price).toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column width="70">
              <template #default="{ $index }"><el-button size="small" type="danger" @click="poForm.items.splice($index, 1)">删</el-button></template>
            </el-table-column>
          </el-table>
          <el-button size="small" type="primary" plain style="margin-top: 8px" @click="poForm.items.push({ goodsId: null, quantity: 1, price: 0 })">+ 添加明细</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="poDlg = false">取消</el-button><el-button type="primary" @click="savePO">保存</el-button></template>
    </el-dialog>

    <!-- 采购单详情弹窗（流程操作） -->
    <el-dialog v-model="poDetailDlg" :title="`采购单 ${poDetail?.orderNo || ''}`" width="900px" top="4vh">
      <template v-if="poDetail">
        <el-descriptions :column="4" border size="small" style="margin-bottom: 12px">
          <el-descriptions-item label="供应商">{{ poDetail.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="金额"><span class="money">{{ poDetail.allAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="含税">{{ poDetail.taxAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small" type="primary">{{ poDetail.status }} / {{ poDetail.auditStatus }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="仓库">{{ poDetail.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="下单日期">{{ poDetail.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="到货日期">{{ poDetail.arrivalDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="阶段">{{ poDetail.orderStates }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="poDetail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="150" />
          <el-table-column prop="quantity" label="数量" width="90" align="right" />
          <el-table-column prop="price" label="单价" width="90" align="right" />
          <el-table-column prop="amount" label="金额" width="100" align="right" />
          <el-table-column prop="receivedQty" label="已到货" width="90" align="right" />
        </el-table>
        <div style="margin-top: 14px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-button type="primary" @click="submitPO">提交审批</el-button>
          <el-button type="success" @click="approvePO(true)">审批通过</el-button>
          <el-button type="warning" @click="approvePO(false)">驳回</el-button>
          <el-button @click="dispatchDlg = true">车辆调度</el-button>
          <el-button type="warning" plain @click="arrivalDlg = true">到货入库</el-button>
          <el-button type="info" plain @click="settlePO">结单</el-button>
        </div>
        <div v-if="followUps.length" style="margin-top: 12px">
          <div style="font-weight: 600; margin-bottom: 6px">采购跟单节点</div>
          <el-timeline>
            <el-timeline-item v-for="f in followUps" :key="f.id" :timestamp="f.operateTime" :type="f.nodeStatus === '已完成' ? 'success' : 'primary'">{{ f.nodeName }} · {{ f.nodeStatus }} · {{ f.operator }}</el-timeline-item>
          </el-timeline>
        </div>
      </template>
      <template #footer><el-button type="primary" @click="poDetailDlg = false">关闭</el-button></template>
    </el-dialog>

    <!-- 车辆调度 -->
    <el-dialog v-model="dispatchDlg" title="车辆调度" width="420px">
      <el-form label-width="90px">
        <el-form-item label="调度车辆">
          <el-select v-model="dispatchForm.vehicleId" style="width: 100%"><el-option v-for="v in vehicles" :key="v.id" :label="`${v.plateNo}(${v.driver})`" :value="v.id" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dispatchDlg = false">取消</el-button><el-button type="primary" @click="doDispatch">确认调度</el-button></template>
    </el-dialog>

    <!-- 到货入库 -->
    <el-dialog v-model="arrivalDlg" title="到货入库（可部分到货）" width="760px">
      <el-form label-width="90px">
        <el-form-item label="入库仓库">
          <el-select v-model="arrivalForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="到货明细">
          <el-table :data="arrivalForm.items" border max-height="240" size="small">
            <el-table-column prop="goodsName" label="商品" min-width="140" />
            <el-table-column prop="quantity" label="计划数量" width="100" align="right" />
            <el-table-column label="实到数量" width="150">
              <template #default="{ row }"><el-input-number v-model="row.realQty" :min="0" :max="row.quantity" :precision="3" size="small" style="width: 100%" /></template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="arrivalDlg = false">取消</el-button><el-button type="primary" @click="doArrival">确认入库</el-button></template>
    </el-dialog>

    <!-- 票据登记 -->
    <el-dialog v-model="billDlg" title="登记采购票据" width="480px">
      <el-form :model="billForm" label-width="100px">
        <el-form-item label="关联采购单">
          <el-select v-model="billForm.orderId" filterable style="width: 100%"><el-option v-for="o in poPage.list" :key="o.id" :label="o.orderNo" :value="o.id" /></el-select>
        </el-form-item>
        <el-form-item label="票据类型">
          <el-select v-model="billForm.billType" style="width: 100%"><el-option label="增值税发票" value="增值税发票" /><el-option label="普通发票" value="普通发票" /><el-option label="收据" value="收据" /></el-select>
        </el-form-item>
        <el-form-item label="票据号码"><el-input v-model="billForm.billNo" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="billForm.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="billDlg = false">取消</el-button><el-button type="primary" @click="saveBill">登记</el-button></template>
    </el-dialog>

    <!-- 销售单编辑弹窗 -->
    <el-dialog v-model="soDlg" :title="soForm.id ? '编辑销售单' : '新建销售单'" width="860px" top="4vh">
      <el-form :model="soForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="客户"><el-select v-model="soForm.customerId" filterable style="width: 100%"><el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="订单日期"><el-date-picker v-model="soForm.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="折扣"><el-input-number v-model="soForm.discount" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="明细">
          <el-table :data="soForm.items" border max-height="260" style="width: 100%">
            <el-table-column label="商品" width="220">
              <template #default="{ row }"><el-select v-model="row.goodsId" filterable size="small" style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select></template>
            </el-table-column>
            <el-table-column label="数量" width="130"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.001" :precision="3" size="small" style="width: 100%" /></template></el-table-column>
            <el-table-column label="单价" width="130"><template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" size="small" style="width: 100%" /></template></el-table-column>
            <el-table-column label="金额" width="110"><template #default="{ row }"><span class="money">{{ (row.quantity * row.price).toFixed(2) }}</span></template></el-table-column>
            <el-table-column width="70"><template #default="{ $index }"><el-button size="small" type="danger" @click="soForm.items.splice($index, 1)">删</el-button></template></el-table-column>
          </el-table>
          <el-button size="small" type="primary" plain style="margin-top: 8px" @click="soForm.items.push({ goodsId: null, quantity: 1, price: 0 })">+ 添加明细</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="soDlg = false">取消</el-button><el-button type="primary" @click="saveSO">保存</el-button></template>
    </el-dialog>

    <!-- 销售单详情弹窗 -->
    <el-dialog v-model="soDetailDlg" :title="`销售单 ${soDetail?.orderNo || ''}`" width="900px" top="4vh">
      <template v-if="soDetail">
        <el-descriptions :column="4" border size="small" style="margin-bottom: 12px">
          <el-descriptions-item label="客户">{{ soDetail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="金额"><span class="money">{{ soDetail.allAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="折扣">{{ soDetail.discount }}</el-descriptions-item>
          <el-descriptions-item label="已收款">{{ soDetail.receivedAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag size="small" type="primary">{{ soDetail.status }} / {{ soDetail.auditStatus }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="订单日期">{{ soDetail.orderDate }}</el-descriptions-item>
          <el-descriptions-item label="交付日期">{{ soDetail.deliveryDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="阶段">{{ soDetail.orderStates }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="soDetail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="150" />
          <el-table-column prop="quantity" label="数量" width="90" align="right" />
          <el-table-column prop="price" label="单价" width="90" align="right" />
          <el-table-column prop="amount" label="金额" width="100" align="right" />
          <el-table-column prop="deliveredQty" label="已出库" width="90" align="right" />
        </el-table>
        <div style="margin-top: 14px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-button type="primary" @click="submitSO">提交审批</el-button>
          <el-button type="success" @click="approveSO(true)">审批通过</el-button>
          <el-button type="warning" @click="approveSO(false)">驳回</el-button>
          <el-button type="warning" plain @click="deliverDlg = true">出库发货</el-button>
        </div>
      </template>
      <template #footer><el-button type="primary" @click="soDetailDlg = false">关闭</el-button></template>
    </el-dialog>

    <!-- 出库发货 -->
    <el-dialog v-model="deliverDlg" title="出库发货（可部分出库）" width="760px">
      <el-form label-width="90px">
        <el-form-item label="出库仓库">
          <el-select v-model="deliverForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="出库明细">
          <el-table :data="deliverForm.items" border max-height="240" size="small">
            <el-table-column prop="goodsName" label="商品" min-width="140" />
            <el-table-column prop="quantity" label="计划数量" width="100" align="right" />
            <el-table-column label="实发数量" width="150"><template #default="{ row }"><el-input-number v-model="row.realQty" :min="0" :max="row.quantity" :precision="3" size="small" style="width: 100%" /></template></el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="deliverDlg = false">取消</el-button><el-button type="primary" @click="doDeliver">确认出库</el-button></template>
    </el-dialog>

    <!-- 退货登记 -->
    <el-dialog v-model="returnDlg" title="登记销售退货" width="520px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="来源销售单">
          <el-select v-model="returnForm.srcId" filterable style="width: 100%" @change="onPickSale"><el-option v-for="o in soPage.list" :key="o.id" :label="o.orderNo" :value="o.id" /></el-select>
        </el-form-item>
        <el-form-item label="退货商品">
          <el-select v-model="returnForm.goodsId" style="width: 100%"><el-option v-for="g in allGoods" :key="g.id" :label="`${g.name}(${g.code})`" :value="g.id" /></el-select>
        </el-form-item>
        <el-form-item label="退货仓库">
          <el-select v-model="returnForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select>
        </el-form-item>
        <el-form-item label="退货数量"><el-input-number v-model="returnForm.quantity" :min="0.001" :precision="3" style="width: 100%" /></el-form-item>
        <el-form-item label="退货金额"><el-input-number v-model="returnForm.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="退货原因"><el-input v-model="returnForm.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="returnDlg = false">取消</el-button><el-button type="primary" @click="saveReturn">登记</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tab = ref('goods')

// ---- 商品 ----
const goodsPage = ref({ total: 0, list: [] })
const goodsQuery = ref({ keyword: '', categoryId: null, page: 1 })
const goodsCats = ref([]), gcatTree = ref([]), units = ref([]), allGoods = ref([])
const goodsDlg = ref(false), goodsForm = ref({})
const loadGoods = async () => { goodsPage.value = await request.get('/goods/list', { params: goodsQuery.value }) }
const loadCats = async () => { gcatTree.value = await request.get('/goods/categories/tree'); goodsCats.value = flatten(gcatTree.value) }
const loadUnits = async () => { units.value = await request.get('/goods/units') }
function flatten(arr) { return arr.flatMap(x => [x, ...(x.children ? flatten(x.children) : [])]) }
function openGoods(row) { goodsForm.value = row ? { ...row } : { status: '在售', lowLimit: 0 }; goodsDlg.value = true }
async function saveGoods() {
  await request.post('/goods/save', goodsForm.value); ElMessage.success('保存成功'); goodsDlg.value = false; loadGoods(); reloadGoods()
}
async function delGoods(row) {
  await ElMessageBox.confirm(`确认删除商品【${row.name}】？`, '提示', { type: 'warning' })
  await request.delete(`/goods/${row.id}`); ElMessage.success('已删除'); loadGoods()
}
const reloadGoods = async () => { allGoods.value = await request.get('/goods/all') }

// ---- 分类/单位 ----
const gcatDlg = ref(false), gcatForm = ref({}), unitDlg = ref(false), unitForm = ref({})
function openGcat(row) { gcatForm.value = row ? { ...row } : {}; gcatDlg.value = true }
async function saveGcat() { await request.post('/goods/categories', gcatForm.value); ElMessage.success('保存成功'); gcatDlg.value = false; loadCats() }
async function delGcat(row) { await ElMessageBox.confirm('确认删除分类？', '提示', { type: 'warning' }); await request.delete(`/goods/categories/${row.id}`); ElMessage.success('已删除'); loadCats() }
function openUnit(row) { unitForm.value = row ? { ...row } : {}; unitDlg.value = true }
async function saveUnit() { await request.post('/goods/units', unitForm.value); ElMessage.success('保存成功'); unitDlg.value = false; loadUnits() }
async function delUnit(row) { await ElMessageBox.confirm('确认删除单位？', '提示', { type: 'warning' }); await request.delete(`/goods/units/${row.id}`); ElMessage.success('已删除'); loadUnits() }

// ---- 采购需求 ----
const demandPage = ref({ total: 0, list: [] }), demandQuery = ref({ status: '', page: 1 })
const demandDlg = ref(false), demandForm = ref({})
const selectedDemands = ref([])
const toOrderDlg = ref(false), toOrderForm = ref({})
const loadDemands = async () => { demandPage.value = await request.get('/purchase/demands', { params: demandQuery.value }) }
function openDemand() { demandForm.value = { quantity: 1, needDate: today() }; demandDlg.value = true }
async function saveDemand() { await request.post('/purchase/demands', demandForm.value); ElMessage.success('保存成功'); demandDlg.value = false; loadDemands() }
async function delDemand(row) { await ElMessageBox.confirm('确认删除需求？', '提示', { type: 'warning' }); await request.delete(`/purchase/demands/${row.id}`); ElMessage.success('已删除'); loadDemands() }
async function doToOrder() {
  await request.post('/purchase/demands/to-order', {
    demandIds: selectedDemands.value.map(d => d.id),
    supplierId: toOrderForm.value.supplierId,
    warehouseId: toOrderForm.value.warehouseId,
    applyDate: toOrderForm.value.applyDate
  })
  ElMessage.success('已生成采购单'); toOrderDlg.value = false; loadDemands(); loadPOs()
}

// ---- 采购单 ----
const poPage = ref({ total: 0, list: [] }), poQuery = ref({ keyword: '', status: '', page: 1 })
const poDlg = ref(false), poForm = ref({ items: [] })
const poDetailDlg = ref(false), poDetail = ref(null)
const dispatchDlg = ref(false), dispatchForm = ref({})
const arrivalDlg = ref(false), arrivalForm = ref({ items: [] })
const followUps = ref([])
const suppliers = ref([]), warehouses = ref([]), vehicles = ref([]), customers = ref([])
const loadPOs = async () => { poPage.value = await request.get('/purchase/orders', { params: poQuery.value }) }
function today() { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }
function openPO(row) {
  poForm.value = row ? JSON.parse(JSON.stringify(row)) : { applyDate: today(), taxRate: 13, items: [] }
  if (!row) poForm.value.items.push({ goodsId: null, quantity: 1, price: 0 })
  poDlg.value = true
}
async function savePO() {
  await request.post('/purchase/orders/save', poForm.value); ElMessage.success('保存成功'); poDlg.value = false; loadPOs()
}
async function delPO(row) { await ElMessageBox.confirm('确认删除采购单？', '提示', { type: 'warning' }); await request.delete(`/purchase/orders/${row.id}`); ElMessage.success('已删除'); loadPOs() }
async function openPODetail(row) {
  poDetail.value = await request.get(`/purchase/orders/${row.id}`)
  followUps.value = await request.get('/purchase/follow-ups', { params: { orderId: row.id } })
  poDetailDlg.value = true
}
async function submitPO() { await request.post(`/purchase/orders/${poDetail.value.id}/submit`); ElMessage.success('已提交审批'); refreshPODetail() }
async function approvePO(pass) {
  const { value } = await ElMessageBox.prompt('审批意见', pass ? '审批通过' : '审批驳回', { inputValue: pass ? '同意' : '驳回原因' }).catch(() => ({ value: '' }))
  await request.post(`/purchase/orders/${poDetail.value.id}/approve`, { pass, comment: value })
  ElMessage.success(pass ? '审批通过' : '已驳回'); refreshPODetail()
}
function refreshPODetail() { openPODetail(poDetail.value) }
function openDispatch() { dispatchForm.value = {}; dispatchDlg.value = true }
async function doDispatch() { await request.post(`/purchase/orders/${poDetail.value.id}/dispatch`, dispatchForm.value); ElMessage.success('调度完成'); dispatchDlg.value = false; refreshPODetail() }
function openArrival() {
  arrivalForm.value = { warehouseId: poDetail.value.warehouseId, items: poDetail.value.items.filter(i => i.receivedQty < i.quantity).map(i => ({ goodsId: i.goodsId, goodsName: i.goodsName, quantity: Number(i.quantity) - Number(i.receivedQty), realQty: Number(i.quantity) - Number(i.receivedQty) })) }
  arrivalDlg.value = true
}
async function doArrival() {
  await request.post(`/purchase/orders/${poDetail.value.id}/arrival`, {
    warehouseId: arrivalForm.value.warehouseId,
    items: arrivalForm.value.items.filter(i => i.realQty > 0).map(i => ({ goodsId: i.goodsId, quantity: i.realQty }))
  })
  ElMessage.success('入库成功'); arrivalDlg.value = false; refreshPODetail(); loadPOs()
}
async function settlePO() {
  await ElMessageBox.confirm('确认对该采购单结单？', '提示', { type: 'warning' })
  await request.post(`/purchase/orders/${poDetail.value.id}/settle`); ElMessage.success('已结单'); refreshPODetail(); loadPOs()
}

// ---- 采购票据 ----
const bills = ref([]), billDlg = ref(false), billForm = ref({})
const loadBills = async () => { bills.value = await request.get('/purchase/bills') }
function openBill() { billForm.value = {}; billDlg.value = true }
async function saveBill() { await request.post('/purchase/bills', billForm.value); ElMessage.success('登记成功'); billDlg.value = false; loadBills() }

// ---- 销售单 ----
const soPage = ref({ total: 0, list: [] }), soQuery = ref({ keyword: '', status: '', page: 1 })
const soDlg = ref(false), soForm = ref({ items: [] })
const soDetailDlg = ref(false), soDetail = ref(null)
const deliverDlg = ref(false), deliverForm = ref({ items: [] })
const loadSOs = async () => { soPage.value = await request.get('/sale/orders', { params: soQuery.value }) }
function openSO(row) {
  soForm.value = row ? JSON.parse(JSON.stringify(row)) : { orderDate: today(), discount: 0, items: [] }
  if (!row) soForm.value.items.push({ goodsId: null, quantity: 1, price: 0 })
  soDlg.value = true
}
async function saveSO() { await request.post('/sale/orders/save', soForm.value); ElMessage.success('保存成功'); soDlg.value = false; loadSOs() }
async function delSO(row) { await ElMessageBox.confirm('确认删除销售单？', '提示', { type: 'warning' }); await request.delete(`/sale/orders/${row.id}`); ElMessage.success('已删除'); loadSOs() }
async function openSODetail(row) {
  soDetail.value = await request.get(`/sale/orders/${row.id}`)
  soDetailDlg.value = true
}
async function submitSO() { await request.post(`/sale/orders/${soDetail.value.id}/submit`); ElMessage.success('已提交审批'); refreshSODetail() }
async function approveSO(pass) {
  const { value } = await ElMessageBox.prompt('审批意见', pass ? '审批通过' : '审批驳回', { inputValue: pass ? '同意' : '驳回原因' }).catch(() => ({ value: '' }))
  await request.post(`/sale/orders/${soDetail.value.id}/approve`, { pass, comment: value })
  ElMessage.success(pass ? '审批通过' : '已驳回'); refreshSODetail()
}
function refreshSODetail() { openSODetail(soDetail.value) }
function openDeliver() {
  deliverForm.value = { warehouseId: soDetail.value.warehouseId, items: soDetail.value.items.filter(i => i.deliveredQty < i.quantity).map(i => ({ goodsId: i.goodsId, goodsName: i.goodsName, quantity: Number(i.quantity) - Number(i.deliveredQty), realQty: Number(i.quantity) - Number(i.deliveredQty) })) }
  deliverDlg.value = true
}
async function doDeliver() {
  await request.post(`/sale/orders/${soDetail.value.id}/deliver`, {
    warehouseId: deliverForm.value.warehouseId,
    items: deliverForm.value.items.filter(i => i.realQty > 0).map(i => ({ goodsId: i.goodsId, quantity: i.realQty }))
  })
  ElMessage.success('出库成功'); deliverDlg.value = false; refreshSODetail(); loadSOs()
}

// ---- 退货 ----
const returnPage = ref({ total: 0, list: [] }), returnQuery = ref({ page: 1 }), returnStatus = ref('')
const returnDlg = ref(false), returnForm = ref({})
const loadReturns = async () => { returnPage.value = await request.get('/sale/returns', { params: { status: returnStatus.value, page: returnQuery.value.page } }) }
function openReturn() { returnForm.value = { quantity: 1, amount: 0 }; returnDlg.value = true }
function onPickSale(id) {
  const so = soPage.value.list.find(o => o.id === id)
  if (so && so.items && so.items.length) {
    const first = so.items[0]
    returnForm.value.goodsId = first.goodsId
    returnForm.value.amount = first.amount
    returnForm.value.quantity = first.quantity
  }
}
async function saveReturn() {
  await request.post('/sale/returns', returnForm.value)
  ElMessage.success('退货登记成功'); returnDlg.value = false; loadReturns()
}

onMounted(async () => {
  loadGoods(); loadCats(); loadUnits(); loadDemands(); loadPOs(); loadSOs(); loadBills(); loadReturns(); reloadGoods()
  const [s, w, v, c] = await Promise.all([
    request.get('/crm/suppliers/all'), request.get('/base/warehouses'),
    request.get('/config/vehicles'), request.get('/crm/customers/all')
  ])
  suppliers.value = s; warehouses.value = w; vehicles.value = v; customers.value = c
})
</script>
