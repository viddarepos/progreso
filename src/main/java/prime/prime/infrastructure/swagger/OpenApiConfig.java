package prime.prime.infrastructure.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer")
public class OpenApiConfig {

    @Value("${infrastructure.mainDomain}")
    private String mainDomain;

    @Value("${infrastructure.apiJava}")
    private String apiJava;

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Progreso")
                        .description("""
                                Progreso is a platform that aims to facilitate the operational tasks in the execution process of the internships \n
                                in Prime Holding. The platform should provide functionalities for keeping track of interns’ progress and feedback, \n
                                absence tracking, and event management.\n
                                It should also be able to offer reports based on past internships.
                                """)
                        .version("v1.0")
                        .termsOfService("TOC"))
                .addServersItem(new Server().url(mainDomain + apiJava));
    }
}
