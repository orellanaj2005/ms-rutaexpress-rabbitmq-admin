package cl.rutaexpress.rabbitmqadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "rutaexpress.rabbitmq")
public class rabbitAdminProperties {

    private List<String> protectedResources = List.of();

    public List<String> getProtected() {
        return protectedResources;
    }

    public void setProtected(List<String> protectedResources) {
        this.protectedResources = protectedResources;
    }

    public boolean isProtected(String name) {
        return name != null && protectedResources.contains(name);
    }
}
