package com.j180.erp.security.dataperm;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.j180.erp.common.enums.DataScopeEnum;
import com.j180.erp.security.UserContext;
import com.j180.erp.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 数据权限处理器（配合 MyBatis-Plus DataPermissionInterceptor 使用）：
 * 在 SQL 执行前根据当前用户上下文动态注入 WHERE 过滤条件（PRD 6.5.3）
 * <ul>
 *   <li>全部（1）：不注入条件；</li>
 *   <li>本部门及子部门（2）：dept_col IN (本部门及全部子孙部门)；</li>
 *   <li>本部门（3）：dept_col IN (本部门)；</li>
 *   <li>本仓库（5）：wh_col IN (员工绑定的仓库)；</li>
 *   <li>本人（4）：self_col = 当前用户（员工/账号，取决于表配置）。</li>
 * </ul>
 * 服务端 SQL 层强制注入，前端隐藏/直接调接口均无法绕过（R-MODEL-7）。
 */
@Slf4j
public class ErpDataPermissionHandler implements DataPermissionHandler {

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        UserContext context = UserContextHolder.get();
        // 无用户上下文（登录等场景）或无需过滤，直接放行
        if (context == null || !context.needDataFilter()) {
            return where;
        }
        TableScope scope = TableScope.resolve(mappedStatementId);
        if (scope == null) {
            return where;
        }
        try {
            DataScopeEnum dataScope = DataScopeEnum.of(context.getDataScope());
            switch (dataScope) {
                case DEPT_AND_CHILD:
                case DEPT:
                    return and(where, inOrNull(scope.getDeptColumn(), context.getDeptScopeIds()));
                case WAREHOUSE:
                    return and(where, inOrNull(scope.getWarehouseColumn(), context.getWarehouseScopeIds()));
                case SELF:
                    return and(where, equalsOrNull(scope.getSelfColumn(), resolveSelfId(context, scope)));
                default:
                    return where;
            }
        } catch (Exception e) {
            // 安全优先：过滤条件构建异常时拒绝返回数据，避免越权数据泄露
            log.error("数据权限条件构建失败，已拒绝查询: statement={}", mappedStatementId, e);
            return and(where, new EqualsTo(new LongValue(1), new LongValue(0)));
        }
    }

    private Long resolveSelfId(UserContext context, TableScope scope) {
        return scope.getSelfIdType() == TableScope.SelfIdType.EMPLOYEE_ID
                ? context.getEmployeeId() : context.getUserId();
    }

    private Expression and(Expression where, Expression condition) {
        if (condition == null) {
            return where;
        }
        return where == null ? condition : new AndExpression(where, condition);
    }

    /**
     * IN 条件：集合为空时返回 1=0（有范围维度但无明细数据，一律拒绝，安全优先）
     */
    private Expression inOrNull(String column, Collection<Long> ids) {
        if (column == null) {
            // 该表不支持此数据维度，不注入
            return null;
        }
        if (ids == null || ids.isEmpty()) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        ExpressionList items = new ExpressionList(ids.stream().map(LongValue::new).collect(Collectors.toList()));
        InExpression in = new InExpression();
        in.setLeftExpression(new Column(column));
        in.setRightItemsList(items);
        return in;
    }

    /**
     * 等值条件：值为空时返回 1=0
     */
    private Expression equalsOrNull(String column, Long value) {
        if (column == null) {
            return null;
        }
        if (value == null) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        return new EqualsTo(new Column(column), new LongValue(value));
    }
}
