package prime.prime.infrastructure.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonDateTimeCustomizer {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer
  jacksonObjectMapperBuilderCustomizer() {
    return jacksonObjectMapperBuilder -> {
      final String dateTimeFormat = "yyyy-MM-dd HH:mm";
      jacksonObjectMapperBuilder
          .serializers(
              new LocalDateTimeSerializer(
                  DateTimeFormatter.ofPattern(dateTimeFormat)))
          .deserializers(
              new LocalDateTimeDeserializer(
                  DateTimeFormatter.ofPattern(dateTimeFormat)));
    };
  }
}
