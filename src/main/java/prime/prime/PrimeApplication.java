package prime.prime;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import prime.prime.domain.technology.mapper.TechnologyMapper;


@SpringBootApplication
public class PrimeApplication {
	public static void main(String[] args) {
		SpringApplication.run(PrimeApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
