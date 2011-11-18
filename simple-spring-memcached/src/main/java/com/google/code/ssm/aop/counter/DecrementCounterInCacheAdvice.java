package com.google.code.ssm.aop.counter;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.aop.AnnotationData;
import com.google.code.ssm.aop.AnnotationDataBuilder;
import com.google.code.ssm.api.counter.DecrementCounterInCache;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author Jakub Białek
 * 
 */
@Aspect
public class DecrementCounterInCacheAdvice extends CounterInCacheBase {

    private static final Logger LOG = LoggerFactory.getLogger(DecrementCounterInCacheAdvice.class);

    @Pointcut("@annotation(com.google.code.ssm.api.counter.DecrementCounterInCache)")
    public void decrementSingleCounter() {
    }

    @AfterReturning("decrementSingleCounter()")
    public void decrementSingle(final JoinPoint jp) throws Throwable {
        // This is injected caching. If anything goes wrong in the caching, LOG
        // the crap outta it, but do not let it surface up past the AOP injection itself.
        // It will be invoked only if underlying method completes successfully.
        String cacheKey;
        DecrementCounterInCache annotation;
        try {
            Method methodToCache = getMethodToCache(jp);
            annotation = methodToCache.getAnnotation(DecrementCounterInCache.class);
            AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation, DecrementCounterInCache.class,
                    methodToCache);
            String[] objectsIds = getObjectIds(annotationData.getKeysIndex(), jp, methodToCache);
            cacheKey = buildCacheKey(objectsIds, annotationData);
            decr(cacheKey, 1);
        } catch (Throwable ex) {
            getLogger().warn(String.format("Decrementing counter on %s aborted due to an error.", jp.toShortString()), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}