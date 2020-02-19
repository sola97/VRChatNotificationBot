package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.service.CookieService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class CommandServiceAspect {
    private static final Logger logger = LoggerFactory.getLogger(CommandServiceAspect.class);
    @Autowired
    CookieService cookieServiceImpl;

    @Pointcut("execution(* cn.sola97.vrchat.service.impl.CommandServiceImpl.*(..))")
    public void CommandServiceMethods() {

    }

    @AfterThrowing(pointcut = "CommandServiceMethods()", throwing = "ex")
    public CommandResultVO doRecoveryActions(JoinPoint joinPoint, Throwable ex) {
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();
        String stuff = signature.toString();
        String arguments = Arrays.toString(joinPoint.getArgs());
        logger.error("We have caught exception in method: "
                + methodName + " with arguments "
                + arguments + "\nand the full toString: " + stuff + "\nthe exception is: "
                + ex.getMessage(), ex);
        return new CommandResultVO().setCode(500).setMsg("服务器内部错误 method:" + methodName + " with arguments " + arguments + "\nand the full toString: " + stuff).setData(ex.getMessage());
    }
}
