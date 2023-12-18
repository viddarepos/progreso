package prime.prime.infrastructure.image.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageValidatorTest {
    private ImageValidator imageValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ValidImage annotation;

    @BeforeEach
    void setUp() {
        imageValidator = new ImageValidator();

        imageValidator.initialize(annotation);
    }

    @Test
    void uploadImage_ValidImage_Successful() throws IOException {
        BufferedImage testImage = ImageIO.read(new File("src/test/resources/images/waves.jpg"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(testImage, "jpg", outputStream);
        MockMultipartFile image = new MockMultipartFile("image.jpg", new ByteArrayInputStream(outputStream.toByteArray()));
        assertTrue(imageValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void uploadImage_InvalidFormat_Violation() throws IOException {
        BufferedImage testImage = ImageIO.read(new File("src/test/resources/images/coffee.gif"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(testImage, "gif", outputStream);
        MockMultipartFile invalidFormatImage = new MockMultipartFile("image.gif", new ByteArrayInputStream(outputStream.toByteArray()));
        String expectedViolationMessage = "Only PNG, JPG, JPEG or HEIC images are allowed";

        when(constraintValidatorContext.getDefaultConstraintMessageTemplate()).thenReturn(expectedViolationMessage);

        assertFalse(imageValidator.isValid(invalidFormatImage, constraintValidatorContext));
        assertEquals(expectedViolationMessage, constraintValidatorContext.getDefaultConstraintMessageTemplate());
    }

    @Test
    void uploadImage_InvalidDimensions_Violation() throws IOException {
        BufferedImage testImage = ImageIO.read(new File("src/test/resources/images/flowers.jpg"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(testImage, "jpg", outputStream);
        MockMultipartFile invalidDimensionsImage = new MockMultipartFile("image.jpg", new ByteArrayInputStream(outputStream.toByteArray()));
        String expectedViolationMessage = "Image dimensions must not exceed 1200x1200.";
        ConstraintViolationBuilder constraintViolationBuilder = mock(ConstraintViolationBuilder.class);
        ConstraintValidatorContext changedContext = mock(ConstraintValidatorContext.class);

        when(constraintValidatorContext.buildConstraintViolationWithTemplate(expectedViolationMessage)).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(changedContext);
        when(changedContext.getDefaultConstraintMessageTemplate()).thenReturn(expectedViolationMessage);

        assertFalse(imageValidator.isValid(invalidDimensionsImage, constraintValidatorContext));
        assertEquals(expectedViolationMessage, changedContext.getDefaultConstraintMessageTemplate());
    }
}