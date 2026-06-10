package com.rikkeibank.aop;

import com.rikkeibank.dto.TransactionDtos.TransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect @Component @Slf4j
public class ExecutionTimeAspect {
    @Around("execution(public * com.rikkeibank.service..*(..))")
    public Object measure(ProceedingJoinPoint p) throws Throwable {
        long start=System.nanoTime();
        try{return p.proceed();}
        finally{log.info("[PERFORMANCE] method={} durationMs={}",p.getSignature().toShortString(),(System.nanoTime()-start)/1_000_000);}
    }
    @AfterReturning(pointcut="execution(* com.rikkeibank.service.TransferService.transfer(..))",returning="result")
    public void transferSuccess(TransactionResponse result){log.info("[AUDIT] Transfer completed code={} from={} to={} amount={} status={}",
        result.transactionCode(),result.fromAccountId(),result.toAccountId(),result.amount(),result.status());}
    @AfterThrowing(pointcut="execution(* com.rikkeibank.service.TransferService.transfer(..))",throwing="e")
    public void transferFailure(JoinPoint p,Throwable e){log.warn("[AUDIT] Transfer failed reason={}",e.getMessage());}
}
