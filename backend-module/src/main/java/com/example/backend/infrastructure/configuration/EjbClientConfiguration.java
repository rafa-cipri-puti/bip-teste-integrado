package com.example.backend.infrastructure.configuration;

import com.example.ejb.service.BeneficioEjbService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Configuration
public class EjbClientConfiguration {

    @Bean
    public Context context(
            @Value("${ejb.provider-url:http-remoting://wildfly:8080}") String providerUrl,
            @Value("${ejb.username:ejbuser}") String username,
            @Value("${ejb.password:ejbpass}") String password
    ) throws NamingException {
        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProps.put(Context.PROVIDER_URL, providerUrl);
        jndiProps.put("jboss.naming.client.ejb.context", "true");
        if (!username.isBlank()) {
            jndiProps.put(Context.SECURITY_PRINCIPAL, username);
        }
        if (!password.isBlank()) {
            jndiProps.put(Context.SECURITY_CREDENTIALS, password);
        }
        jndiProps.put("jboss.naming.client.ejb.context", true);
        return new InitialContext(jndiProps);
    }

    @Bean
    public BeneficioEjbService beneficioEjbService(
            Context context,
            @Value("${ejb.jndi-name:app/com.example-ejb-module-0.0.1-SNAPSHOT/BeneficioEjbServiceImpl!com.example.ejb.service.BeneficioEjbService}") String jndiName
            ) throws Exception {
        return (BeneficioEjbService) context.lookup(jndiName);
    }
}
