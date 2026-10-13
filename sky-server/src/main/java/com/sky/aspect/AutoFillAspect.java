package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.entity.AuditedEntity;
import com.sky.enumeration.OperationType;
import com.sky.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {
    private final Clock clock;
    public AutoFillAspect(Clock clock) { this.clock = clock; }

    @Before("@annotation(autoFill)")
    public void fill(JoinPoint call, AutoFill autoFill) {
        Long actor = BaseContext.getCurrentId();
        if (actor == null) throw new BusinessException(403, "写入操作缺少员工身份");
        if (call.getArgs().length != 1 || !(call.getArgs()[0] instanceof AuditedEntity entity)) {
            throw new IllegalStateException("@AutoFill requires one AuditedEntity argument");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (autoFill.value() == OperationType.INSERT) {
            entity.setCreateTime(now); entity.setCreateUser(actor);
        }
        entity.setUpdateTime(now); entity.setUpdateUser(actor);
    }
}
