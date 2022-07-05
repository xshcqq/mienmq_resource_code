package com.mienmq.client.config;

import com.mienmq.client.annotation.EnableMienMq;
import com.mienmq.client.annotation.MienMqListener;
import com.mienmq.client.client.NettyInvokeClientFactoryBean;
import com.mienmq.client.client.constants.ConnectionConstants;
import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.constants.ConsumerConstants;
import com.mienmq.client.client.exception.BusinessException;
import com.mienmq.client.enums.ClientBizErrorInfo;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @describe which class have order {@link EnableMienMq} could enable to auto load mienmq
 * @see com.mienmq.client.client.NettyClient
 */
public class EnableMienMqRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, ApplicationContextAware, ApplicationRunner {

    private ApplicationContext context;
    private Environment environment;

    private ResourceLoader resourceLoader;

    private static final HashMap annotationAttributes = new HashMap();

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            AtomicInteger threadNum = new AtomicInteger(0);
            return new Thread("executorService_Thread" + threadNum);
        }
    }
    );


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        if (!metadata.hasAnnotation(EnableMienMq.class.getName())) throw new BusinessException(ClientBizErrorInfo.ENABLEMIENTMQ_NOT_FIND);
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableMienMq.class.getName());
        this.annotationAttributes.putAll(annotationAttributes);

        Set<String> basePackages;
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        Map<String, Object> attrs = metadata
                .getAnnotationAttributes(EnableMienMq.class.getName());
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                MienMqListener.class);
        final Class<?>[] clients = attrs == null ? null
                : (Class<?>[]) attrs.get("clients");
        if (clients == null || clients.length == 0) {
            scanner.addIncludeFilter(annotationTypeFilter);
            basePackages = getBasePackages(metadata);
        }
        else {
            final Set<String> clientClasses = new HashSet<>();
            basePackages = new HashSet<>();
            for (Class<?> clazz : clients) {
                basePackages.add(ClassUtils.getPackageName(clazz));
                clientClasses.add(clazz.getCanonicalName());
            }
            AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
                @Override
                protected boolean match(ClassMetadata metadata) {
                    String cleaned = metadata.getClassName().replaceAll("\\$", ".");
                    return clientClasses.contains(cleaned);
                }
            };
            scanner.addIncludeFilter(
                    new AllTypeFilter(Arrays.asList(filter, annotationTypeFilter)));
        }
        // 从每个包中找到包含@mienmqlistener注解的类，并注册到Spring
        Map<String, String> consumerAnnotationInfo = new ConcurrentHashMap<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();

                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(
                                    MienMqListener.class.getCanonicalName());
                    consumerAnnotationInfo.put(annotationMetadata.getClassName(), (String) attributes.get(ConsumerConstants.QUEUE_NAME));
                    // 注册消息监听类
                    registerMienMqListener(registry, annotationMetadata, attributes);
                }
            }
        }

        // 注册客户端连接配置类
        // 注册netty
        registryNettyClient(registry, consumerAnnotationInfo);
    }

    /**
     * 初始化客户端连接配置类
     * {@link ConnectionInitConfiguration}
     */
    private ConnectionInitConfiguration makeConnectionConfig() {
        String host = environment.getProperty(ConnectionConstants.host);
        String port = environment.getProperty(ConnectionConstants.port);
        if (StringUtil.isNullOrEmpty(host) || StringUtil.isNullOrEmpty(port)) {
            throw new BusinessException(ClientBizErrorInfo.UNKNOWN, "Netty服务端地址无效！");
        }
        boolean whetherReconnect = Boolean.TRUE;
        String whetherReconnectStr = environment.getProperty(ConnectionConstants.whetherReconnect);
        if (!StringUtil.isNullOrEmpty(whetherReconnectStr)) {
            whetherReconnect = Boolean.getBoolean(whetherReconnectStr);
        }
        String times = environment.getProperty(ConnectionConstants.retryTimes);
        int retryTimes = 0;
        if (!StringUtil.isNullOrEmpty(times)) {
            retryTimes = Integer.parseInt(times);
            if (whetherReconnect && retryTimes < ConsumerConstants.minRetryTimes){
                throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "客户端断线重试次数配置有误！");
            }
        }
        String pullMsgNumStr = environment.getProperty(ConnectionConstants.pullMsgNum);
        int pullMsgNum = ConsumerConstants.defaultPullMessageNum; // 默认每次拉取32条消息
        if (!StringUtil.isNullOrEmpty(pullMsgNumStr)) {
            pullMsgNum = Integer.parseInt(pullMsgNumStr);
            if (pullMsgNum < ConsumerConstants.minPullMessageNum || pullMsgNum > ConsumerConstants.maxPullMessageNum){
                throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "每次拉取消息数量配置有误！");
            }
        }
        String consumeFailMaxTimesStr = environment.getProperty(ConnectionConstants.consumeFailMaxTimes);
        int consumeFailMaxTimes = 0;
        if (!StringUtil.isNullOrEmpty(consumeFailMaxTimesStr)) {
            consumeFailMaxTimes = Integer.parseInt(consumeFailMaxTimesStr);
            if (consumeFailMaxTimes > ConsumerConstants.maxConsumeFailMaxTimes){
                throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "消费消息最大失败次数！");
            }
        }
        return new ConnectionInitConfiguration(host, port, whetherReconnect, retryTimes, pullMsgNum, consumeFailMaxTimes);
    }

    /**
     * 注册Netty客户端Bean
     * {@link NettyInvokeClientFactoryBean}
     * @param registry
     */
    private void registryNettyClient (BeanDefinitionRegistry registry, Map<String, String> consumerAnnotationInfo) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setTargetType(NettyInvokeClientFactoryBean.class);
        beanDefinition.setBeanClass(NettyInvokeClientFactoryBean.class);
        // 给服务端连接配置类赋值
        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        propertyValues.add("constants", makeConnectionConfig());
        propertyValues.add("consumerAnnotationInfo", consumerAnnotationInfo);

        registry.registerBeanDefinition(NettyInvokeClientFactoryBean.class.getCanonicalName(), beanDefinition);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * 启动注解上如果存在就赋值覆盖ymal中的用户配置的
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ConnectionInitConfiguration bean = context.getBean(ConnectionInitConfiguration.class);
        if (annotationAttributes.containsKey("whetherReconnect")) {
            bean.setWhetherReconnect((Boolean) annotationAttributes.get("whetherReconnect"));
        }
        if (annotationAttributes.containsKey("retryTimes")) {
            bean.setRetryTimes((Integer) annotationAttributes.get("retryTimes"));
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * 注册单个带有{@link MienMqListener}注解的消费者
     * @param registry
     * @param annotationMetadata
     * @param attributes
     */
    private void registerMienMqListener(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(annotationMetadata.getClassName());
        registry.registerBeanDefinition(annotationMetadata.getClassName(), beanDefinition);
    }

    /**
     * 获取扫描包路径
     * @param importingClassMetadata
     * @return
     */
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Set<String> basePackages = new HashSet<>();
        basePackages.add(
                ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        return basePackages;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    private static class AllTypeFilter implements TypeFilter {

        private final List<TypeFilter> delegates;

        /**
         * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
         * @param delegates must not be {@literal null}.
         */
        AllTypeFilter(List<TypeFilter> delegates) {
            Assert.notNull(delegates, "This argument is required, it must not be null");
            this.delegates = delegates;
        }

        @Override
        public boolean match(MetadataReader metadataReader,
                             MetadataReaderFactory metadataReaderFactory) throws IOException {

            for (TypeFilter filter : this.delegates) {
                if (!filter.match(metadataReader, metadataReaderFactory)) {
                    return false;
                }
            }
            return true;
        }
    }

}
