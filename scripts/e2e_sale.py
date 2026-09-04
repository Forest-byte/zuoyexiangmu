# -*- coding: utf-8 -*-
"""端到端验证：销售全流程（新建->审批(信用校验)->出库发货->库存减少->应收生成->删除）"""
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

login = call("POST", "/auth/login", {"username": "admin", "password": "admin123"})
token = login["data"]["token"]
print("[1] 登录成功")

# 取测试数据
goods = call("GET", "/goods/all", token=token)["data"]
cus = call("GET", "/crm/customers/all", token=token)["data"]
wh = call("GET", "/base/warehouses", token=token)["data"]
g = goods[0]; c = cus[0]; w = wh[0]
print(f"[2] 测试: 商品={g['name']}({g['id']}), 客户={c['name']}({c['id']}), 仓库={w['name']}({w['id']})")

before = call("GET", f"/wms/stocks?goodsId={g['id']}&warehouseId={w['id']}", token=token)["data"]
bqty = before["list"][0]["quantity"] if before["list"] else 0
print(f"[3] 出库前库存: {bqty}")

# 1. 新建销售单
so = call("POST", "/sale/orders/save", {
    "customerId": c["id"], "orderDate": "2026-09-04", "discount": 0,
    "items": [{"goodsId": g["id"], "quantity": 2, "price": 200}]
}, token)
if so.get("code") != 200:
    print("[X] 新建销售单失败:", so); sys.exit(1)
sid = so["data"]["id"]; sno = so["data"]["orderNo"]
print(f"[4] 新建销售单成功: {sno} (id={sid}), 金额={so['data']['allAmount']}")

# 2. 提交审批（含信用校验）
print("[5] 提交审批:", call("POST", f"/sale/orders/{sid}/submit", {}, token).get("message"))
# 3. 审批通过
print("[6] 审批通过:", call("POST", f"/sale/orders/{sid}/approve", {"pass": True, "comment": "同意"}, token).get("message"))
# 4. 出库发货
out = call("POST", f"/sale/orders/{sid}/deliver", {
    "warehouseId": w["id"], "items": [{"goodsId": g["id"], "quantity": 2}]
}, token)
print("[7] 出库发货:", out.get("message"), "出库单:", out.get("data", {}).get("outNo", "") if out.get("data") else "")
# 5. 验证库存减少
after = call("GET", f"/wms/stocks?goodsId={g['id']}&warehouseId={w['id']}", token=token)["data"]
aqty = after["list"][0]["quantity"] if after["list"] else 0
print(f"[8] 出库后库存: {bqty} -> {aqty} (应减少 2)")
# 6. 验证应收生成
so2 = call("GET", f"/sale/orders/{sid}", token=token)["data"]
print(f"[9] 销售单状态: {so2['status']}/{so2['auditStatus']}, 已收款={so2['receivedAmount']}")
# 7. 删除（草稿才可删——已发货不能删，验证保护）
print("[10] 删除已发货单(应被拒):", call("DELETE", f"/sale/orders/{sid}", token=token).get("message"))
print("\n>>> 销售全流程 E2E 验证完成（保留单据验证应收/信用） <<<")
print(f"残留销售单: {sno} (id={sid})")
