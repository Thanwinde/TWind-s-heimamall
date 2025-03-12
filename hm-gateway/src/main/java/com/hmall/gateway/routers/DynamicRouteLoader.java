package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;

    private final String dataId ="gateway-routes.json";

    private final String group ="DEFAULT_GROUP";

    private Set<String> OldRoute = new HashSet<>();

    private final RouteDefinitionWriter routeDefinitionWriter;


    @PostConstruct
    public void init() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
            @Override
            public Executor getExecutor() {
                return Executors.newFixedThreadPool(2);
            }

            @Override
            public void receiveConfigInfo(String s) {
                updateConfigInfo(s);
            }
        });
        updateConfigInfo(configInfo);
    }

    private void updateConfigInfo(String configInfo) {
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        log.info("routeDefinitions: {}", routeDefinitions);

        OldRoute.forEach(
                a -> routeDefinitionWriter.delete(Mono.just(a)).subscribe());
        OldRoute.clear();
        for(RouteDefinition routeDefinition : routeDefinitions) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            OldRoute.add(routeDefinition.getId());
        }
    }
}
