# -*- coding: utf-8 -*-
"""端到端验证：采购全流程（需求->采购单->审批->调度->到货入库->库存增加->结单）"""
import json, urllib.request, sys

BASE = "http://localhost:8080/api"
def call(method, path, body=None, token=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token: req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return json.loads(e.read().decode("utf-8"))

# 登录
login = call("POST", "/auth/login", {"username": "admin", "password": "admin123"})
token = login["data"]["token"]
print("[1] 登录成功")

# 取一个商品/仓库/供应商
goods = call("GET", "/goods/all", token=token)["data"]
sup = call("GET", "/crm/suppliers/all", token=token)["data"]
wh = call("GET", "/base/warehouses", token=token)["data"]
g = goods[0]; s = sup[0]; w = wh[0]
print(f"[2] 测试数据: 商品={g['name']}(id={g['id']}), 供应商={s['name']}(id={s['id']}), 仓库={w['name']}(id={w['id']})")

# 库存快照
before = call("GET", f"/wms/stocks?goodsId={g['id']}&warehouseId={w['id']}", token=token)["data"]
bqty = before["list"][0]["quantity"] if before["list"] else 0
print(f"[3] 入库前库存: {bqty}")

# 1. 新建采购单
po = call("POST", "/purchase/orders/save", {
    "supplierId": s["id"], "warehouseId": w["id"], "applyDate": "2026-09-04",
    "taxRate": 13, "remark": "E2E测试单",
    "items": [{"goodsId": g["id"], "quantity": 5, "price": 100}]
}, token)
if po.get("code") != 200:
    print("[X] 新建采购单失败:", po); sys.exit(1)
pid = po["data"]["id"]; pno = po["data"]["orderNo"]
print(f"[4] 新建采购单成功: {pno} (id={pid})")

# 2. 提交审批
print("[5] 提交审批:", call("POST", f"/purchase/orders/{pid}/submit", {}, token).get("message"))
# 3. 审批通过
print("[6] 审批通过:", call("POST", f"/purchase/orders/{pid}/approve", {"pass": True, "comment": "同意"}, token).get("message"))
# 4. 车辆调度
vehicles = call("GET", "/config/vehicles", token=token)["data"]
if vehicles:
    print("[7] 车辆调度:", call("POST", f"/purchase/orders/{pid}/dispatch", {"vehicleId": vehicles[0]["id"]}, token).get("message"))
# 5. 到货入库
arr = call("POST", f"/purchase/orders/{pid}/arrival", {
    "warehouseId": w["id"], "items": [{"goodsId": g["id"], "quantity": 5}]
}, token)
print("[8] 到货入库:", arr.get("message"), "入库单:", arr.get("data", {}).get("inNo", "") if arr.get("data") else "")

# 6. 验证库存增加
after = call("GET", f"/wms/stocks?goodsId={g['id']}&warehouseId={w['id']}", token=token)["data"]
aqty = after["list"][0]["quantity"] if after["list"] else 0
print(f"[9] 入库后库存: {bqty} -> {aqty} (应增加 5)")

# 7. 跟单节点
ups = call("GET", f"/purchase/follow-ups?orderId={pid}", token=token)["data"]
print(f"[10] 跟单节点数: {len(ups)} ->", [u["nodeName"] + ":" + u["nodeStatus"] for u in ups])

# 8. 结单
print("[11] 结单:", call("POST", f"/purchase/orders/{pid}/settle", {}, token).get("message"))

# 清理测试数据
print("[12] 清理: 删除测试采购单", call("DELETE", f"/purchase/orders/{pid}", token=token).get("message"))
print("\n>>> 采购全流程 E2E 验证完成 <<<")
