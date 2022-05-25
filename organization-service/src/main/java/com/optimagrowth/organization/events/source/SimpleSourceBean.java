package com.optimagrowth.organization.events.source;

import com.optimagrowth.organization.events.model.OrganizationChangeModel;
import com.optimagrowth.organization.utils.UserContextHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SimpleSourceBean {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSourceBean.class);
    // private Source source;

    // public SimpleSourceBean(Source source) {
    // this.source = source;
    // }
    private StreamBridge bridge;

    public SimpleSourceBean(StreamBridge bridge) {
        this.bridge = bridge;
    }

    public void publishOrganizationChange(ActionEnum action, String organizationId) {
        logger.debug("Sending Kafka message {} for Organization Id: {}", action, organizationId);
        OrganizationChangeModel change = new OrganizationChangeModel(
                OrganizationChangeModel.class.getTypeName(),
                action.toString(),
                organizationId,
                UserContextHolder.getContext().getCorrelationId());
        // source.output().send(MessageBuilder.withPayload(change).build());
        bridge.send("orgChange-out-0", MessageBuilder.withPayload(change).build());
    }
}