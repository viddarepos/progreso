package prime.prime;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping
@Hidden
public class TestController {

    private final RestTemplate restTemplate;

    @Value("${infrastructure.gatewayDomain}")
    private String gatewayDomain;

    @Value("${infrastructure.apiGateway}")
    private String apiGateway;

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/test")
    public String testSimple() {
        return "Java Api is OK";
    }

    @GetMapping("/testJavaGatewayCommunication")
    public String test() {
        log.info("hello test");

        var uri = gatewayDomain + apiGateway + "/test";

        Object response = restTemplate.getForObject(
                uri, String.class);

        log.info(response.toString());

        return response.toString();
    }
}

