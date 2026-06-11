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

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {
    @Around("execution(public * com.rikkeibank.service..*(..))")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[PERFORMANCE] method={} durationMs={}", joinPoint.getSignature().toShortString(), durationMs);
        }
    }

    @AfterReturning(
        pointcut = "execution(* com.rikkeibank.service.TransferService.transfer(..))",
        returning = "result"
    )
    public void transferSuccess(TransactionResponse result) {
        log.info(
            "[AUDIT] Transfer completed code={} from={} to={} amount={} status={}",
            result.transactionCode(),
            result.fromAccountId(),
            result.toAccountId(),
            result.amount(),
            result.status()
        );
    }

    @AfterThrowing(
        pointcut = "execution(* com.rikkeibank.service.TransferService.transfer(..))",
        throwing = "exception"
    )
    public void transferFailure(JoinPoint joinPoint, Throwable exception) {
        log.warn("[AUDIT] Transfer failed method={} reason={}", joinPoint.getSignature().toShortString(), exception.getMessage());
    }
}
