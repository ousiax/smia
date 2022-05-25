package com.optimagrowth.license;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.optimagrowth.license.events.model.OrganizationChangeModel;
import com.optimagrowth.license.utils.UserContextInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient
@EnableFeignClients
// @EnableBinding(Sink.class)
public class LicensingServiceApplication {
	private Logger logger = LoggerFactory.getLogger(LicensingServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LicensingServiceApplication.class, args);
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(Locale.US);
		return localeResolver;
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setBasenames("messages");
		return messageSource;
	}

	@LoadBalanced
	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate template = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
		if (interceptors == null) {
			template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
		} else {
			interceptors.add(new UserContextInterceptor());
			template.setInterceptors(interceptors);
		}
		return template;
	}

	// @StreamListener(Sink.INPUT)
	// public void loggerSink(OrganizationChangeModel orgChange) {
	// logger.debug("Received an {} event for organization id {}",
	// orgChange.getAction(),
	// orgChange.getOrganizationId());
	// }

	@Bean
	public Consumer<OrganizationChangeModel> orgChange() {
		return orgChange -> logger.debug(
				"Received an {} event for organization id {}", orgChange.getAction(),
				orgChange.getOrganizationId());
	}
}
